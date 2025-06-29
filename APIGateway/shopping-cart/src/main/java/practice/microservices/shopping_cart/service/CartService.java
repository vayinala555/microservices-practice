package practice.microservices.shopping_cart.service;

import practice.microservices.shopping_cart.entities.CartItem;

import java.util.List;

public interface CartService {
    CartItem addItem(String userId, CartItem item);
    List<CartItem> getActiveItems(String userId);
    void removeItem(Long itemId);
    void clearActiveCart(String userId);
    double getActiveCartTotal(String userId);
    void markCartAsOrdered(String userId);
}
