package dev.borjessons.helidon.react.template.notify;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Channel {
  USER_CACHE("user_cache"),
  SESSION_CACHE("session_cache");

  private static final Map<String, Channel> STRING_TO_CHANNEL = Stream.of(values()).collect(Collectors.toMap(Object::toString, e -> e));

  private final String name;

  Channel(String name) {
    this.name = name;
  }

  public static Optional<Channel> fromString(String name) {
    return Optional.ofNullable(STRING_TO_CHANNEL.get(name));
  }

  @Override
  public String toString() {
    return name;
  }
}
