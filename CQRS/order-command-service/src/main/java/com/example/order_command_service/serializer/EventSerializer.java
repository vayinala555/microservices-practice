package com.example.order_command_service.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String serialize(Object event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    public <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

}
