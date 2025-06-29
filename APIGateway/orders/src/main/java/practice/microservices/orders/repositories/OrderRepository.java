package practice.microservices.orders.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.microservices.orders.entities.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
}
