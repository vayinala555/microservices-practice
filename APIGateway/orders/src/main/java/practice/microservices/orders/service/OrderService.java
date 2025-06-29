package practice.microservices.orders.service;

import practice.microservices.orders.entities.Order;

import java.util.List;

public interface OrderService {
    Order placeOrder(String userId);
    List<Order> getOrdersByUserId(String userId);
    Order getOrderByOrderId(Long userId);
}
