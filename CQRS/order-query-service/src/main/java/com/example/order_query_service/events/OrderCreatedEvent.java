package com.example.order_query_service.events;

import lombok.Data;

@Data
public class OrderCreatedEvent {
    private String orderId;
    private String customerId;
    private String product;
    private int quantity;
    private double amount;

}
