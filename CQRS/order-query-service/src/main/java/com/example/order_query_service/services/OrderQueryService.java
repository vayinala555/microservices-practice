package com.example.order_query_service.services;

import com.example.order_query_service.entities.OrderDocument;
import com.example.order_query_service.repositories.OrderMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderQueryService {

    @Autowired
    private OrderMongoRepository orderMongoRepository;

    public List<OrderDocument> getOrders(String userId) {
        return orderMongoRepository.findByCustomerId(userId);
    }

    public OrderDocument createOrder(OrderDocument orderDocument) {
        // This method is not typically used in a query service, but included for completeness
        // In a real-world scenario, this would be handled by the command service
        return orderMongoRepository.save(orderDocument);
    }
}
