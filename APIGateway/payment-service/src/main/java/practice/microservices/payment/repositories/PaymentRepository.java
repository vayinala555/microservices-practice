package practice.microservices.payment.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.microservices.payment.entities.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
}
