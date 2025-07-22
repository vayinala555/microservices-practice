package practice.microservices.orders.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String userId;
    private String orderId;
    private double amount;
    private String paymentMethod; // e.g., "CREDIT_CARD", "PAYPAL", etc.
}
