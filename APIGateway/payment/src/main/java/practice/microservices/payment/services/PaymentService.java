package practice.microservices.payment.services;

import practice.microservices.payment.dto.PaymentRequest;
import practice.microservices.payment.entities.Payment;

public interface PaymentService {
    Payment processPayment(PaymentRequest request);
    String getStatus(String orderId);
}
