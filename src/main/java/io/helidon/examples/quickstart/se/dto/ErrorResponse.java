package io.helidon.examples.quickstart.se.dto;

public record ErrorResponse(String message, String details) {
  public static ErrorResponse of(String message) {
    return new ErrorResponse(message, "");
  }

  public static ErrorResponse of(String message, String details) {
    return new ErrorResponse(message, details);
  }
}
