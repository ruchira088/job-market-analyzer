package com.ruchij.crawler.dao.elasticsearch.models;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public record EncryptedText(String value) {
	public static final JsonSerializer<EncryptedText> jsonSerializer =
		new JsonSerializer<>() {
			@Override
			public void serialize(EncryptedText encryptedText, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
				jsonGenerator.writeString(encryptedText.value());
			}
		};

	public static final JsonDeserializer<EncryptedText> jsonDeserializer =
		new JsonDeserializer<>() {
			@Override
			public EncryptedText deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
				return new EncryptedText(jsonParser.getText());
			}
		};
}
