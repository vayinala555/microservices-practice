package com.example.OrderService.services;

import com.example.OrderService.OrderStatus;
import com.example.OrderService.entities.Order;
import com.example.OrderService.repositories.OrderRepository;
import com.example.OrderService.requests.CreateOrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    OrderRepository repo;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate kafkaTemplate;

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(request.getTotal());
        order.setItemsJson(serialize(request.getItems()));
        Order savedOrder = repo.save(order);

        // publish OrderCreated
        Map<String,Object> payload = Map.of(
                "type","OrderCreated",
                "orderId", order.getOrderId(),
                "items", request.getItems(),
                "total", order.getTotal()
        );
        kafkaTemplate.send("order-events",payload);
        return savedOrder;
    }

    // handlers for orchestrator commands
    @KafkaListener(topics = "order-events", groupId = "order-service-group")
    public void onOrderEvent(String msg) {
        JsonNode node = parse(parse(msg).asText());
        String type = node.get("type").asText();
        if ("CompleteOrder".equals(type)) {
            String orderId = node.get("orderId").asText();
            repo.findByOrderId(orderId).
                    ifPresent(o -> {
                o.setStatus(OrderStatus.COMPLETED);
                repo.save(o);
                // publish OrderCompleted
                kafkaTemplate.send("order-events", toJson(Map.of("type","OrderCompleted","orderId",orderId)));
            });
        } else if ("CancelOrder".equals(type)) {
            String orderId = node.get("orderId").asText();
            repo.findByOrderId(orderId).ifPresent(o -> {
                o.setStatus(OrderStatus.CANCELLED);
                repo.save(o);
                kafkaTemplate.send("order-events", toJson(Map.of("type","OrderCancelled","orderId",orderId)));
            });
        }
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing object to JSON", e);
        }
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON message", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
