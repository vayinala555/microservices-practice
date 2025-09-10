package com.example.OrderService.entities;

import com.example.OrderService.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;
    private String orderId; // business id
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private double total;
    // simple JSON string for items or separate table
    private String itemsJson;
}

