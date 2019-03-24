package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entity.Product;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class contains methods that are responsible for connecting to URL, parsing HTML and creating JSON file.
 */
@Data
@Log4j2
public class WebShopParser {

    public static final String ATTRIBUTE_VALUE_TILE_DEFAULT = "ProductTileDefault";
    public static final String ATTRIBUTE_VALUE_BRAND_NAME = "ProductBrandName";
    public static final String ATTRIBUTE_VALUE_PRODUCT_NAME = "ProductName";
    public static final String ATTRIBUTE_VALUE_PRODUCT_PRICES = "ProductPrices";
    public static final String ATTRIBUTE_VALUE_VARIANT_COLOR = "VariantColor";
    public static final String ATTRIBUTE_VALUE_ARTICLE_NUMBER = "_articleNumber_1474d";
    public static final String KEY_ATTRIBUTE_DATA_TEST_ID = "data-test-id";
    public static final String KEY_ATTRIBUTE_CLASS = "class";
    public static final String KEY_ATTRIBUTE_HREF = "href";
    public static final String TAG_A = "a";
    public static final String BASE_URL = "https://www.aboutyou.de";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36";
    private static final String GOOGLE_COM = "http://www.google.com";

    private AtomicInteger requestAmount = new AtomicInteger(0);
    private long timeForAllRequests;

    /**
     * Method execute connection to URL and return Document (DOM) object.
     *
     * @param url path to web shop to parce.
     * @return Document (HTML form) object.
     */
    public Document getDocFromURL(String url) {
        long startTime = System.currentTimeMillis();
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .timeout(0)//use only when bad connection to Internet in order not to have timeout exception;
                    .userAgent(USER_AGENT) //provide possibility to overcome spam-filters
                    .referrer(GOOGLE_COM) //provide simulation of manual google search
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            log.debug("Can not achieve source web shop", e);
        }
        long currentTime = System.currentTimeMillis();
        timeForAllRequests += (currentTime - startTime);
        requestAmount.incrementAndGet();
        return doc;
    }

    /**
     * Method take the Document object and make some selection according to the given constants to parse information
     * about each product from webshop and create a dto of the product with initialized fields. It returns collection
     * (List) of Products.
     *
     * @param doc Document object (HTML form).
     * @return parameterized List of Product (with initialized fields).
     */
    public List<Product> parseHtmlDoc(Document doc, ExecutorService service) {
        final List<Product> productList = new CopyOnWriteArrayList<Product>();
        Elements productTileDefault = doc.getElementsByAttributeValue(KEY_ATTRIBUTE_DATA_TEST_ID, ATTRIBUTE_VALUE_TILE_DEFAULT);
        for (final Element element : productTileDefault) {
            service.execute(new Runnable() {
                public void run() {
                    Product product = new Product();
                    Elements productBrandName = element.getElementsByAttributeValue(KEY_ATTRIBUTE_DATA_TEST_ID, ATTRIBUTE_VALUE_BRAND_NAME);
                    product.setProductBrandName(productBrandName.text());
                    Elements productName = element.getElementsByAttributeValue(KEY_ATTRIBUTE_DATA_TEST_ID, ATTRIBUTE_VALUE_PRODUCT_NAME);
                    product.setProductName(productName.text());
                    Elements productPrice = element.getElementsByAttributeValue(KEY_ATTRIBUTE_DATA_TEST_ID, ATTRIBUTE_VALUE_PRODUCT_PRICES);
                    product.setProductPrice(productPrice.text());
                    //go to the deeper layer (link to each product to take article number and color)
                    String link = element.select(TAG_A).first().attr(KEY_ATTRIBUTE_HREF);
                    Document secondLayerDoc = getDocFromURL(BASE_URL.trim() + link.trim());
                    Elements articleNumbers = secondLayerDoc.getElementsByAttributeValue(KEY_ATTRIBUTE_CLASS, ATTRIBUTE_VALUE_ARTICLE_NUMBER);
                    product.setArticleNumber(articleNumbers.text());
                    Elements color = secondLayerDoc.getElementsByAttributeValue(KEY_ATTRIBUTE_DATA_TEST_ID, ATTRIBUTE_VALUE_VARIANT_COLOR);
                    product.setColor(color.text());
                    productList.add(product);
                }
            });
        }
        try {
            if(!service.awaitTermination(30, TimeUnit.SECONDS)){
                service.shutdown();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            e.printStackTrace();
        }
        return productList;
    }

    /**
     * Method take parameterized collection List of Product and path to create JSON file and create JSON file.
     *
     * @param list List of Product - collection with products.
     * @param path type String path is a path where to create new JSON file.
     */
    public void createJsonDocWithProducts(List<Product> list, String path) {
        File productFile = new File(path);
        FileWriter fileWriter;
        ObjectMapper objectMapper;
        try {
            fileWriter = new FileWriter(productFile);
            objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String dataToWriteInFile = objectMapper.writeValueAsString(list);
            fileWriter.write(dataToWriteInFile);
            fileWriter.close();
        } catch (IOException e) {
            log.debug("Can not create file", e);
        }
    }
}
