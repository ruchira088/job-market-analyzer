package com.ruchij.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ruchij.dao.elasticsearch.models.EncryptedText;

public class JsonUtils {
    public static final ObjectMapper objectMapper = objectMapper();

    private static ObjectMapper objectMapper() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(EncryptedText.class, EncryptedText.jsonSerializer);
        simpleModule.addDeserializer(EncryptedText.class, EncryptedText.jsonDeserializer);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }
}
