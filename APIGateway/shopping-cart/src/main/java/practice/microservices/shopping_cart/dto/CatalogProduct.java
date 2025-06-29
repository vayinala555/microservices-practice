package practice.microservices.shopping_cart.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CatalogProduct {
    private Long id;
    private String name;
    private double price;

}
