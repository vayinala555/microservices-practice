package practice.microservices.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import practice.microservices.payment.dto.PaymentRequest;
import practice.microservices.payment.entities.Payment;
import practice.microservices.payment.services.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentContrloller {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<Payment> pay(@RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> getStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getStatus(orderId));
    }
}
