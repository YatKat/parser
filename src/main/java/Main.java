import entity.Product;
import lombok.extern.log4j.Log4j2;
import parser.WebShopParser;

import java.io.File;
import java.util.List;

@Log4j2
public class Main {

    public static void main(String[] args) {
        WebShopParser parser = new WebShopParser();
        List<Product> list;
        String PATH = "products.json";
        list = parser.executeParserInMultiThreadsEnv();
        File fileJson = parser.createJsonDocWithProducts(list, PATH);
        System.out.println("-------------------------------------------------");
        System.out.println("Amount of products at the site: " + list.size());
        System.out.println("Amount of HTTP requests: " + parser.getRequestAmount());
        System.out.println("Whole time taken for HTTP requests: " + parser.getTimeForAllRequests() + " ms.");
        System.out.println("-------------------------------------------------");
    }
}
