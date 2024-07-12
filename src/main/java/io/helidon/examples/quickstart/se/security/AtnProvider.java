package io.helidon.examples.quickstart.se.security;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.examples.quickstart.se.data.repository.SessionRepository;
import io.helidon.security.AuthenticationResponse;
import io.helidon.security.ProviderRequest;
import io.helidon.security.SecurityResponse;
import io.helidon.security.spi.AuthenticationProvider;

public class AtnProvider implements AuthenticationProvider {
  static final Pattern SESSION_PATTERN = Pattern.compile("JSESSION=([\\w|-]{36})");
  private static final Logger logger = LoggerFactory.getLogger(AtnProvider.class);

  private final Clock clock;
  private final SessionRepository sessionRepository;

  public AtnProvider() {
    clock = Clock.systemUTC();
    sessionRepository = Contexts.globalContext().get(SessionRepository.class).orElseThrow();
  }

  private static AuthenticationResponse createFailureResponse() {
    return AuthenticationResponse.builder()
        .description("authentication failed")
        .throwable(null)
        .status(SecurityResponse.SecurityStatus.FAILURE)
        .responseHeaders(Map.of("WWW-Authenticate", List.of("")))
        .build();
  }

  private static Optional<String> getSessionIdOption(List<String> stringList) {
    return stringList
        .stream()
        .map(SESSION_PATTERN::matcher)
        .filter(Matcher::find)
        .map(matcher -> matcher.group(1))
        .findFirst();
  }

  @Override
  public AuthenticationResponse authenticate(ProviderRequest providerRequest) {
    logger.debug("ENTERED AUTH PROVIDER");

    return getSessionIdOption(providerRequest.env().headers().getOrDefault("Cookie", List.of()))
        .flatMap(this::getValidSession)
        .map(session -> AuthenticationResponse.success(null, null))
        .orElseGet(AtnProvider::createFailureResponse);
  }

  private Optional<Session> getValidSession(String sessionId) {
    return sessionRepository.findById(UUID.fromString(sessionId))
        .filter(this::isSessionValid);
  }

  private boolean isSessionValid(Session session) {
    return clock.instant().isBefore(session.expires().toInstant());
  }
}
