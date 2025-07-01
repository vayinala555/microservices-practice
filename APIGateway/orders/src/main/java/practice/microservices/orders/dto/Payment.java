package practice.microservices.orders.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class Payment {
    private String orderId;
    private String userId;
    private double amount;
    private String status;
    private LocalDateTime paymentDate;
}
