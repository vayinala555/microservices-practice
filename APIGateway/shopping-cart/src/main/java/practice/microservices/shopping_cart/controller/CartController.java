package practice.microservices.shopping_cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import practice.microservices.shopping_cart.dto.CatalogProduct;
import practice.microservices.shopping_cart.entities.CartItem;
import practice.microservices.shopping_cart.enums.CartStatus;
import practice.microservices.shopping_cart.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${catalog.service.url}")
    String catalogServiceUrl;

    @PostMapping("/{userId}/add")
    public ResponseEntity<CartItem> addItem(@PathVariable String userId, @RequestBody CartItem item) {
        String url = catalogServiceUrl + "/product/" + item.getProductId();
        CatalogProduct product = restTemplate.getForObject(url, CatalogProduct.class);
        if (product == null) {
            throw new RuntimeException("Product not found in Catalog");
        }
        item.setPrice(product.getPrice());
        item.setProductName(product.getName());
        item.setStatus(CartStatus.ACTIVE);
        return new ResponseEntity<CartItem>(cartService.addItem(userId, item), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getActiveItems(userId));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearActiveCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/total")
    public ResponseEntity<Double> getTotal(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getActiveCartTotal(userId));
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkoutCart(@PathVariable String userId) {
        cartService.markCartAsOrdered(userId);
        return ResponseEntity.ok("Cart marked as ORDERED");
    }
}
