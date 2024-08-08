package dev.borjessons.helidon.react.template.utils;

import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public final class JsonUtils {
  private static final Jsonb JSONB = JsonbBuilder.create();

  private JsonUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static <T> List<T> fromJsonList(JsonArray jsonArray, Class<T> type) {

    return jsonArray.stream()
        .map(JsonValue::asJsonObject)
        .map(jsonObject -> JSONB.fromJson(jsonObject.toString(), type))
        .toList();
  }
}
