package practice.microservices.shopping_cart.repositiories;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.microservices.shopping_cart.entities.CartItem;
import practice.microservices.shopping_cart.enums.CartStatus;

import java.util.List;

public interface CartRepositiory extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdAndStatus(String userId, CartStatus status);
    void deleteByUserIdAndStatus(String userId, CartStatus status);
}
