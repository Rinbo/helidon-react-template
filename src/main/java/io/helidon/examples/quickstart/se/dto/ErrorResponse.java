package io.helidon.examples.quickstart.se.dto;

import java.util.Optional;

public record ErrorResponse(String message, String details) {
  public static ErrorResponse of(String message) {
    return new ErrorResponse(Optional.ofNullable(message).orElse(""), "");
  }

  public static ErrorResponse of(String message, String details) {
    return new ErrorResponse(Optional.ofNullable(message).orElse("unknown error"), Optional.ofNullable(details).orElse(""));
  }
}
