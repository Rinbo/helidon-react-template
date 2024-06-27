package io.helidon.examples.quickstart.se.utils;

import java.io.InputStream;
import java.util.List;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class TestUtils {
  private static class JsonbType<T> {
    public java.lang.reflect.Type getType() {
      return new com.fasterxml.jackson.core.type.TypeReference<T>() {
      }.getType();
    }
  }

  private static final Jsonb JSONB = JsonbBuilder.create();

  public static <T> List<T> fromJsonList(InputStream jsonStream, Class<T> type) {
    return JSONB.fromJson(jsonStream, new JsonbType<List<T>>() {
    }.getType());
  }
}
