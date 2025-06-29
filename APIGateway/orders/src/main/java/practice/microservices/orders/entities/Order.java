package practice.microservices.orders.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    long orderId;

    private String userId;
    private LocalDateTime orderDate;
    private double totalAmount;

    @OneToMany(mappedBy = "order",  cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> itemsList;

}
