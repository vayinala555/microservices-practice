package com.example.order_query_service.controller;

import com.example.order_query_service.entities.OrderDocument;
import com.example.order_query_service.services.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderQueryController {

    @Autowired
    private OrderQueryService orderQueryService;

    @GetMapping("/orders/{userId}")
    public List<OrderDocument> getOrders(@PathVariable String userId) {
        return orderQueryService.getOrders(userId);
    }

    @PostMapping("/orders")
    public OrderDocument createOrder(@RequestBody OrderDocument orderDocument) {
        System.out.println("orderDocument.getQuantity() " + orderDocument.getQuantity()
                +" orderDocument.getProduct() " + orderDocument.getProduct() +
                " orderDocument.getCustomerId() " + orderDocument.getCustomerId() +
                " orderDocument.getAmount() " + orderDocument.getAmount() +
                " orderDocument.getStatus() " + orderDocument.getStatus());
        // This method is not typically used in a query service, but included for completeness
        // In a real-world scenario, this would be handled by the command service
        return orderQueryService.createOrder(orderDocument);
    }
}
