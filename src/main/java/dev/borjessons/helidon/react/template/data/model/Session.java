package dev.borjessons.helidon.react.template.data.model;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record Session(UUID id, int userId, ZonedDateTime expires) {
  public Session {
    Objects.requireNonNull(id, "id is required");
    Objects.requireNonNull(expires, "expires is required");
  }
}
