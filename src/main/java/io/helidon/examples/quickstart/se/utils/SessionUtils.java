package io.helidon.examples.quickstart.se.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.helidon.config.Config;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.http.SetCookie;

public final class SessionUtils {
  private static final Pattern SESSION_PATTERN = Pattern.compile("JSESSION=([\\w|-]{36})");

  private SessionUtils() {
    throw new IllegalStateException("utility class");
  }

  public static SetCookie createCookie(Session session) {
    return SetCookie.builder(Constants.COOKIE_SESSION_NAME, session.id().toString())
        .path("/")
        .expires(Instant.now().plusSeconds(Constants.SESSION_DURATION.toSeconds()))
        .httpOnly(true)
        .secure(!Config.global().get("app.profile").asString().orElse("unknown").equals("dev"))
        .sameSite(SetCookie.SameSite.STRICT)
        .build();
  }

  public static SetCookie createLogoutCookie() {
    return SetCookie.builder(Constants.COOKIE_SESSION_NAME, null)
        .path("/")
        .maxAge(Duration.ZERO)
        .httpOnly(true)
        .secure(!Config.global().get("app.profile").asString().orElse("unknown").equals("dev"))
        .sameSite(SetCookie.SameSite.STRICT)
        .build();
  }

  public static Optional<String> getSessionIdOption(List<String> cookiesStrings) {
    return cookiesStrings
        .stream()
        .map(SESSION_PATTERN::matcher)
        .filter(Matcher::find)
        .map(matcher -> matcher.group(1))
        .findFirst();
  }
}
