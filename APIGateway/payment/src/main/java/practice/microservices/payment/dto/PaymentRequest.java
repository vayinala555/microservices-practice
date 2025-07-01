package practice.microservices.payment.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private String orderId;
    private double amount;
}
