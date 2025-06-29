package practice.microservices.shopping_cart.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import practice.microservices.shopping_cart.entities.CartItem;
import practice.microservices.shopping_cart.enums.CartStatus;
import practice.microservices.shopping_cart.repositiories.CartRepositiory;
import practice.microservices.shopping_cart.service.CartService;

import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepositiory repo;

    @Override
    public CartItem addItem(String userId, CartItem item) {
        item.setUserId(userId);
        item.setStatus(CartStatus.ACTIVE);
        return repo.save(item);
    }

    @Override
    public List<CartItem> getActiveItems(String userId) {
        return repo.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
    }

    @Override
    public void removeItem(Long itemId) {
        repo.deleteById(itemId);
    }

    @Override
    public void clearActiveCart(String userId) {
        repo.deleteByUserIdAndStatus(userId, CartStatus.ACTIVE);
    }

    @Override
    public double getActiveCartTotal(String userId) {
        return repo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public void markCartAsOrdered(String userId) {
        List<CartItem> items = repo.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        for (CartItem item : items) {
            item.setStatus(CartStatus.ORDERED);
        }
        repo.saveAll(items);
    }

}
