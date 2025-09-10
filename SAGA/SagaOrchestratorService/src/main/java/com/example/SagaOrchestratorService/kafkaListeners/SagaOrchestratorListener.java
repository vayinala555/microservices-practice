package com.example.SagaOrchestratorService.kafkaListeners;

import com.example.SagaOrchestratorService.entity.SagaState;
import com.example.SagaOrchestratorService.repositioties.SagaStateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SagaOrchestratorListener {

    @Autowired
    KafkaTemplate<String,String> kafka;

    @Autowired
    SagaStateRepository sagaRepo;

    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "saga-orchestrator-group")
    public void onOrderEvent(String msg) {
        JsonNode node = parse(msg);
        String type = node.get("type").asText();
        String orderId = node.path("orderId").asText();
        if ("OrderCreated".equalsIgnoreCase(type)) {
            SagaState saga = new SagaState();
            saga.setOrderId(orderId);
            saga.setState("STARTED");
            sagaRepo.save(saga);

            // now trigger inventory
            kafka.send("inventory-events",toJson(Map.of("type","ReserveInventory",
                    "orderId", orderId,
                    "total",node.get("total"),
                    "items", node.get("items"))));
        } else if ("OrderCompleted".equalsIgnoreCase(type)) {
             sagaRepo.findByOrderId(orderId).ifPresent( saga ->
             {
                 saga.setState("COMPLETED");
                 saga.setLastEvent("OrderCompleted");
                 sagaRepo.save(saga);
             });
        } else if ("OrderCancelled".equalsIgnoreCase(type)) {
            sagaRepo.findByOrderId(orderId).ifPresent(saga -> {
                saga.setState("ORDER_CANCELLED");
                sagaRepo.save(saga);
            });
        }

    }

    @KafkaListener(topics = "inventory-events", groupId = "saga-orchestrator-group")
    public void onIventoryEvent(String msg) {
        JsonNode node = parse(parse(msg).asText());
        String type = node.get("type").asText();
        String orderId = node.path("orderId").asText();
        if ("InventoryReserved".equalsIgnoreCase(type)) {
            sagaRepo.findByOrderId(orderId).ifPresent(saga -> {
                saga.setState("INVENTORY_RESERVED");
                saga.setLastEvent(type);
                sagaRepo.save(saga);
            });
            kafka.send("payment-events", toJson(Map.of(
                    "type","ChargePayment","orderId",orderId,"amount", node.path("total").asDouble()
            )));
        } else if ("InventoryReservationFailed".equalsIgnoreCase(type) || "InventoryReleased".equalsIgnoreCase(type)) {
            kafka.send("order-events", toJson(Map.of("type", "CancelOrder", "orderId", orderId)));
            sagaRepo.findByOrderId(orderId).ifPresent(saga -> {
                saga.setState("FAILED");
                saga.setLastEvent(type);
                sagaRepo.save(saga);
            });
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "saga-orchestrator-group")
    public void onPaymentEvent(String msg) {
        JsonNode node = parse(parse(msg).asText());
        String type = node.get("type").asText();
        String orderId = node.path("orderId").asText();
        if ("PaymentCompleted".equalsIgnoreCase(type)) {
            kafka.send("order-events", toJson(Map.of("type","CompleteOrder","orderId",orderId)));
            sagaRepo.findByOrderId(orderId).ifPresent(saga -> {
                saga.setState("PAYMENT_COMPLETED");
                saga.setLastEvent(type);
                sagaRepo.save(saga);
            });
        } else if("PaymentFailed".equals(type)) {
            kafka.send("inventory-events", toJson(Map.of("type","ReleaseInventory",
                    "orderId",orderId, "items",
                    node.path("items"))));

            sagaRepo.findByOrderId(orderId).ifPresent(saga -> {
                saga.setState("FAILED");
                saga.setLastEvent(type);
                sagaRepo.save(saga);
            });
        }
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON message", e);
        }
    }
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    }
