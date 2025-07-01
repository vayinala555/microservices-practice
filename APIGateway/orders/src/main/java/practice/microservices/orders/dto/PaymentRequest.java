package practice.microservices.orders.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private String orderId;
    private double amount;
}
