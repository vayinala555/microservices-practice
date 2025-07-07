package practice.microservices.orders.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CartItemDto {
    private Long productId;
    private String productName;
    private double price;
    private int quantity;
}
