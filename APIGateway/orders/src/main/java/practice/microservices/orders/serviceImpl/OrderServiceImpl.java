package practice.microservices.orders.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import practice.microservices.orders.dto.CartItemDto;
import practice.microservices.orders.entities.Order;
import practice.microservices.orders.entities.OrderItem;
import practice.microservices.orders.repositories.OrderItemRepository;
import practice.microservices.orders.repositories.OrderRepository;
import practice.microservices.orders.service.OrderService;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    OrderItemRepository orderItemRepo;

    @Value("${cart.service.url}")
    String cartServiceUrl;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Order placeOrder(String userId) {

        ResponseEntity<CartItemDto[]> response = restTemplate.getForEntity(cartServiceUrl + "/" + userId, CartItemDto[].class);

        CartItemDto[] cartItems = response.getBody();

        if (cartItems == null || cartItems.length == 0) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(java.time.LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItemDto item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
            total += item.getPrice() * item.getQuantity();
        }

        order.setTotalAmount(total);
        order.setItemsList(orderItems);

        Order orderResult = orderRepo.save(order);


        // Mark cart items as ORDERED
        String checkoutUrl = cartServiceUrl + "/" + userId + "/checkout";
        restTemplate.postForObject(checkoutUrl, null, String.class);

        return orderResult;

    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepo.findByUserId(userId);
    }

    @Override
    public Order getOrderByOrderId(Long orderId) {
        return orderRepo.findById(orderId).get();
    }
}
