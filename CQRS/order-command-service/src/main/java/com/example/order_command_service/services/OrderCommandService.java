package com.example.order_command_service.services;

import com.example.order_command_service.entities.OrderEvent;
import com.example.order_command_service.events.OrderCreatedEvent;
import com.example.order_command_service.repositiories.OrderEventRepository;
import com.example.order_command_service.serializer.EventSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderCommandService {

    @Autowired
    EventSerializer eventSerializer;

    @Autowired
    OrderEventRepository orderEventRepository;

    @Autowired
    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void createOrder(OrderCreatedEvent orderCreatedEvent) {
        try {
            // Serialize the event
            String serializedEvent = eventSerializer.serialize(orderCreatedEvent);

            // Save the event to the database
            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setOrderId(orderCreatedEvent.getOrderId());
            orderEvent.setEventType("ORDER_CREATED");
            orderEvent.setPayload(serializedEvent);
            orderEvent.setTimestamp(LocalDateTime.now());
            orderEventRepository.save(orderEvent);

            // Publish the event to Kafka

            OrderCreatedEvent orderEventToSend = new OrderCreatedEvent();
            orderEventToSend.setOrderId(orderCreatedEvent.getOrderId());
            orderEventToSend.setCustomerId(orderCreatedEvent.getCustomerId());
            orderEventToSend.setProduct(orderCreatedEvent.getProduct());
            orderEventToSend.setQuantity(orderCreatedEvent.getQuantity());
            orderEventToSend.setAmount(orderCreatedEvent.getAmount());
            kafkaTemplate.send("order-events", orderEventToSend);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception (e.g., log it, rethrow it, etc.)
        }
    }
}
