package com.example.InventoryService.kafka;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import com.example.InventoryService.entities.Inventory;
import com.example.InventoryService.repositories.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryListener {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    InventoryRepository repo;

    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory-events", groupId = "inventory-service-group")
    public void onInventoryCommand(String message) {
        JsonNode node = parse(parse(message).asText());
        parse(message);
        System.out.println("node = " + node);
        String type = node.get("type").asText();
        String orderId = node.path("orderId").asText();

        if ("ReserveInventory".equals(type)) {
            JsonNode items = node.get("items");
            boolean ok = true;
            for(JsonNode it: items){
                String sku = it.get("sku").asText();
                int qty = it.get("qty").asInt();
                Inventory inv = repo.findById(sku).orElseThrow();
                if(inv.getAvailable() < qty) { ok = false; break; }
            }
            if(ok) {
                for(JsonNode item: items) {
                    String sku = item.get("sku").asText();
                    int qty = item.get("qty").asInt();
                    Inventory inv = repo.findById(item.get("sku").asText()).get();
                    inv.setAvailable(inv.getAvailable() - item.get("qty").asInt());
                    repo.save(inv);
                }
                kafkaTemplate.send("inventory-events", toJson(Map.of("type","InventoryReserved","orderId",orderId,"total",node.get("total"))));
            } else  {
                kafkaTemplate.send("inventory-events", toJson(Map.of("type","InventoryReservationFailed","orderId",orderId)));
            }
        } else if ("ReleaseInventory".equals(type)) {
            JsonNode items = node.get("items");
            for (JsonNode item: items) {
                String sku = item.get("sku").asText();
                Inventory inv = repo.findById(item.get("sku").asText()).get();
                inv.setAvailable(inv.getAvailable() + item.get("qty").asInt());
                repo.save(inv);
            }
            kafkaTemplate.send("inventory-events", toJson(Map.of("type","InventoryReleased","orderId",orderId)));
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
