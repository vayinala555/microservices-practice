package practice.microservices.orders.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.microservices.orders.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
