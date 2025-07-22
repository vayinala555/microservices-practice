package practice.microservices.orders.serviceImpl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import practice.microservices.orders.dto.CartItemDto;
import practice.microservices.orders.dto.Payment;
import practice.microservices.orders.dto.PaymentRequest;
import practice.microservices.orders.entities.Order;
import practice.microservices.orders.entities.OrderItem;
import practice.microservices.orders.repositories.OrderItemRepository;
import practice.microservices.orders.repositories.OrderRepository;
import practice.microservices.orders.service.OrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    OrderItemRepository orderItemRepo;

    @Value("${cart.service.url}")
    String cartServiceUrl;

    @Value("${payment.service.url}")
    String paymentServiceUrl;


    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Order placeOrder(String userId, String paymentMethod) {

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
        order.setPaymentStatus("PENDING");
        Order savedOrder = orderRepo.save(order);


        // Mark cart items as ORDERED
        String checkoutUrl = cartServiceUrl + "/" + userId + "/checkout";
        restTemplate.postForObject(checkoutUrl, null, String.class);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(String.valueOf(savedOrder.getOrderId()));
        paymentRequest.setAmount(total);
        paymentRequest.setUserId(userId);

        String paymentStatus = processPayment(paymentRequest);
        Order orderToUpdate = orderRepo.findById(savedOrder.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        orderToUpdate.setPaymentStatus(paymentStatus);
        return orderRepo.save(orderToUpdate);

    }

    @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "handlePaymentFailure")
    public String processPayment(PaymentRequest paymentRequest) {
        String paymentUrl = paymentServiceUrl + "/pay";
        Payment paymentResult = null;
        try {
            paymentResult = restTemplate.postForObject(paymentUrl, paymentRequest, Payment.class);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }

        return ("SUCCESS".equals(paymentResult.getStatus())) ? "SUCCESS" : "FAILED";
    }

    public String handlePaymentFailure(PaymentRequest request, Throwable t) {
        System.out.println("Fallback triggered: Payment service failed. " + t.getMessage());
        return "FAILED";
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
