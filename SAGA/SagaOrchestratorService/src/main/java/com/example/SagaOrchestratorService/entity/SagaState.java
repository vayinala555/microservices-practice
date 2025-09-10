package com.example.SagaOrchestratorService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class SagaState {
    @Id
    @GeneratedValue
    private Long id;
    private String orderId;
    private String state; // e.g., INVENTORY_RESERVED, PAYMENT_COMPLETED, FAILED
    private String lastEvent;
    private LocalDateTime updatedAt;
    // getters/setters
}
