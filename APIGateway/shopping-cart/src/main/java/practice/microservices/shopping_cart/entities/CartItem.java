package practice.microservices.shopping_cart.entities;

import jakarta.persistence.*;
import lombok.Data;
import practice.microservices.shopping_cart.enums.CartStatus;

@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long productId;
    private int quantity;
    private double price;
    private String userId;
    private String productName;

    @Enumerated(EnumType.STRING)
    private CartStatus status = CartStatus.ACTIVE; // Default value;
}
