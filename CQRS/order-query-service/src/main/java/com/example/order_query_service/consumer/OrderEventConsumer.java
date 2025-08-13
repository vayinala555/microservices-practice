package com.example.order_query_service.consumer;

import com.example.order_query_service.entities.OrderDocument;
import com.example.order_query_service.events.OrderCreatedEvent;
import com.example.order_query_service.repositories.OrderMongoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    @Autowired
    private OrderMongoRepository mongoRepo;

    @KafkaListener(topics = "order-events", groupId = "order-group")
    public void listen(String message) {
        ObjectMapper mapper = new ObjectMapper();
        OrderCreatedEvent event = null;
        try {
            event = mapper.readValue(message, OrderCreatedEvent.class);

            OrderDocument doc = new OrderDocument();
            doc.setOrderId(event.getOrderId());
            doc.setCustomerId(event.getCustomerId());
            doc.setProduct(event.getProduct());
            doc.setAmount(event.getAmount());
            doc.setQuantity(event.getQuantity());
            doc.setStatus("CREATED");

            mongoRepo.save(doc);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}
