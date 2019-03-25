package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productName;
    private String productBrandName;
    private String productPrice;
    private String articleNumber;
    private String color;
}
