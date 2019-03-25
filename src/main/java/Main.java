import entity.Product;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import parser.WebShopParser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Main {
    private static WebShopParser parser = new WebShopParser();
    private static List<Product> list;

    public static void main(String[] args) {
        final String URL = "https://www.aboutyou.de/maenner/bekleidung";
        String PATH = "products.json";
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(new Runnable() {
            public void run() {
                Document doc = parser.getDocFromURL(URL);
                list = parser.parseHtmlDoc(doc);
            }
        });
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdown();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            log.error("Thread interrupted", e);
        }

        parser.createJsonDocWithProducts(list, PATH);
        System.out.println("-------------------------------------------------");
        System.out.println("Amount of products at the site: " + list.size());
        System.out.println("Amount of HTTP requests: " + parser.getRequestAmount());
        System.out.println("Whole time taken for HTTP requests: " + parser.getTimeForAllRequests() + " ms.");
        System.out.println("-------------------------------------------------");
    }
}
