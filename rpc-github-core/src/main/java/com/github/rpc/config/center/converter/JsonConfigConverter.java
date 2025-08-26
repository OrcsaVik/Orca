package com.github.rpc.config.center.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class JsonConfigConverter<T> implements ConfigConverter<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Class<T> type;

    public JsonConfigConverter() {
    }

    public JsonConfigConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public T convert(String value) {
        if (value == null) {
            return null;
        }
        if (type == null) {
            throw new IllegalStateException("Target type is not set for JsonConfigConverter");
        }
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (IOException e) {
            log.error("Failed to convert JSON to object", e);
            throw new IllegalArgumentException("Failed to convert JSON to object", e);
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}