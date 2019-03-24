import entity.Product;
import org.jsoup.nodes.Document;
import parser.WebShopParser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        String URL = "https://www.aboutyou.de/maenner/bekleidung";
        String PATH = "products.json";
        WebShopParser parser = new WebShopParser();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Document doc = parser.getDocFromURL(URL);
        List<Product> list = parser.parseHtmlDoc(doc, executor);
        parser.createJsonDocWithProducts(list, PATH);
        System.out.println("-------------------------------------------------");
        System.out.println("Amount of products at the site: " + list.size());
        System.out.println("Amount of HTTP requests are: " + parser.getRequestAmount());
        System.out.println("Whole time taken for HTTP requests are: " + parser.getTimeForAllRequests() + " ms.");
    }
}
