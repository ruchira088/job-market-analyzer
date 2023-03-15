package com.ruchij.crawler.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.ruchij.crawler.dao.elasticsearch.models.EncryptedText;

import java.io.IOException;
import java.time.Instant;

public class JsonUtils {
	public static final ObjectMapper objectMapper = objectMapper();

	private static ObjectMapper objectMapper() {
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(EncryptedText.class, EncryptedText.jsonSerializer);
		simpleModule.addDeserializer(EncryptedText.class, EncryptedText.jsonDeserializer);
		simpleModule.addSerializer(Instant.class, new InstantSerializer());
		simpleModule.addDeserializer(Instant.class, new InstantDeserializer());

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(simpleModule);

		return objectMapper;
	}

	private static class InstantSerializer extends JsonSerializer<Instant> {
		@Override
		public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
			jsonGenerator.writeString(instant.toString());
		}
	}

	private static class InstantDeserializer extends JsonDeserializer<Instant> {
		@Override
		public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			String isoTimestamp = jsonParser.getValueAsString();
			return Instant.parse(isoTimestamp);
		}
	}
}
