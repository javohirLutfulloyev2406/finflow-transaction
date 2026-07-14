package com.finflow.transaction.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Jackson wrapper.
 *
 * Service qatlami ObjectMapper API'siga bog'lanib qolmasligi uchun.
 */
@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ExceptionWithStatusCode(
                    500,
                    "Unable to serialize object to JSON"
            );
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ExceptionWithStatusCode(
                    500,
                    "Unable to deserialize JSON"
            );
        }
    }
}