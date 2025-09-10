package com.example.PaymentService.kafkaListeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor // to avoid autowire
public class PaymentListener {
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "payment-service-group")
    public void onPaymentCommand(String msg) {
        JsonNode node = parse(parse(msg).asText());
        String type = node.get("type").asText();
        String orderId = node.path("orderId").asText();
        double amount = node.path("amount").asDouble();

        if ("ChargePayment".equals(type)) {
            boolean processed = initiatePayment(amount);
            if (processed) {
                kafka.send("payment-events",  toJson(Map.of("type","PaymentCompleted","orderId",orderId)));
            } else {
                kafka.send("payment-events",  toJson(Map.of("type","PaymentFailed","orderId",orderId, "reason","Declined due to max limit")));
            }
        }

    }

    private boolean initiatePayment(double amount) {
        // we cannot process larger amounts
        if( amount > 100000) {
            return false ;
        }
        return true;
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
