package com.example.order_query_service.controller;

import com.example.order_query_service.entities.OrderDocument;
import com.example.order_query_service.services.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderQueryController {

    @Autowired
    private OrderQueryService orderQueryService;

    @GetMapping("/orders/{userId}")
    public List<OrderDocument> getOrders(@PathVariable String userId) {
        return orderQueryService.getOrders(userId);
    }
}
