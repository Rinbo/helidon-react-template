package dev.borjessons.helidon.react.template.utils;

import java.time.Duration;

public final class Constants {
  public static final String COOKIE_SESSION_NAME = "JSESSION";
  public static final Duration LOGIN_PASSCODE_VALIDITY_PERIOD = Duration.ofMinutes(5);
  public static final short MAX_LOGIN_ATTEMPTS = 5;
  public static final Duration SESSION_DURATION = Duration.ofDays(30);

  private Constants() {
    throw new IllegalStateException("Utility class");
  }
}
