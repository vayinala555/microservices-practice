package com.example.order_command_service.controller;

import com.example.order_command_service.events.OrderCreatedEvent;
import com.example.order_command_service.services.OrderCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderCommandService commandService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody OrderCreatedEvent dto) throws Exception {
        commandService.createOrder(dto);
        return ResponseEntity.ok("Order Created");
    }
}
