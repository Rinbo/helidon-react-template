package io.helidon.examples.quickstart.se.utils;

import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class TestUtils {
  private static final Jsonb JSONB = JsonbBuilder.create();

  public static <T> List<T> fromJsonList(JsonArray jsonArray, Class<T> type) {
    return jsonArray.stream()
        .map(JsonValue::asJsonObject)
        .map(jsonObject -> JSONB.fromJson(jsonObject.toString(), type))
        .toList();
  }
}
