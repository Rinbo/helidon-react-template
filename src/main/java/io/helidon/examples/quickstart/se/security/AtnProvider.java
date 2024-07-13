package io.helidon.examples.quickstart.se.security;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.examples.quickstart.se.data.model.Role;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.data.repository.SessionRepository;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.utils.SessionUtils;
import io.helidon.security.AuthenticationResponse;
import io.helidon.security.Grant;
import io.helidon.security.Principal;
import io.helidon.security.ProviderRequest;
import io.helidon.security.SecurityResponse;
import io.helidon.security.Subject;
import io.helidon.security.spi.AuthenticationProvider;

public class AtnProvider implements AuthenticationProvider {
  private static final Logger logger = LoggerFactory.getLogger(AtnProvider.class);

  private final Clock clock;
  private final SessionRepository sessionRepository;
  private final UserRepository userRepository;

  public AtnProvider() {
    clock = Clock.systemUTC();
    sessionRepository = Contexts.globalContext().get(SessionRepository.class).orElseThrow();
    userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
  }

  private static AuthenticationResponse createFailureResponse() {
    return AuthenticationResponse.builder()
        .description("authentication failed")
        .throwable(null)
        .status(SecurityResponse.SecurityStatus.FAILURE)
        .responseHeaders(Map.of("WWW-Authenticate", List.of("")))
        .build();
  }

  private static Principal createPrincipal(User user) {
    return Principal.builder()
        .id(user.email())
        .name(user.name())
        .build();
  }

  private static Function<Role, Grant> createRoleGrant() {
    return role -> Grant.builder()
        .name(role.name())
        .type("role")
        .build();
  }

  private static Subject createSubject(User user) {
    Subject.Builder subjectBuilder = Subject.builder()
        .addPrincipal(createPrincipal(user));

    user.roles()
        .stream()
        .map(createRoleGrant())
        .forEach(subjectBuilder::addGrant);

    return subjectBuilder.build();
  }

  @Override
  public AuthenticationResponse authenticate(ProviderRequest providerRequest) {
    logger.debug("ENTERED AUTH PROVIDER");

    return SessionUtils.getSessionIdOption(providerRequest.env().headers().getOrDefault("Cookie", List.of()))
        .flatMap(this::getValidSession)
        .flatMap(this::getUser)
        .map(user -> AuthenticationResponse.success(createSubject(user), null))
        .orElseGet(AtnProvider::createFailureResponse);
  }

  private Optional<User> getUser(Session session) {
    return userRepository.findById(session.userId());
  }

  private Optional<Session> getValidSession(String sessionId) {
    return sessionRepository.findById(UUID.fromString(sessionId))
        .filter(this::isSessionValid);
  }

  private boolean isSessionValid(Session session) {
    return clock.instant().isBefore(session.expires().toInstant());
  }
}
