package io.helidon.examples.quickstart.se.security;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.data.repository.AuthRepository;
import io.helidon.examples.quickstart.se.data.repository.SessionRepository;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.dto.LoginForm;
import io.helidon.examples.quickstart.se.utils.SessionUtils;
import io.helidon.examples.quickstart.se.utils.Validate;
import io.helidon.http.HeaderNames;
import io.helidon.http.HttpException;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class AuthService implements HttpService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final AuthRepository authRepository;
  private final SessionRepository sessionRepository;
  private final UserRepository userRepository;

  public AuthService(AuthRepository authRepository, SessionRepository sessionRepository, UserRepository userRepository) {
    Objects.requireNonNull(authRepository, "authRepository is required");
    Objects.requireNonNull(sessionRepository, "sessionRepository is required");
    Objects.requireNonNull(userRepository, "userRepository is required");

    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.authRepository = authRepository;
  }

  public AuthService() {
    Context context = Contexts.globalContext();
    authRepository = context.get(AuthRepository.class).orElseThrow();
    sessionRepository = context.get(SessionRepository.class).orElseThrow();
    userRepository = context.get(UserRepository.class).orElseThrow();
  }

  @Override
  public void routing(HttpRules rules) {
    rules.post("/login", this::login);
    rules.post("/authenticate", this::authenticate);
    rules.post("/logout", this::logout);
  }

  private void authenticate(ServerRequest request, ServerResponse response) {
    String email = request.prologue().query().first("email").orElseThrow(() -> new HttpException("email missing", Status.BAD_REQUEST_400));
    String loginToken = request.prologue().query().first("token").orElseThrow(() -> new HttpException("login-token missing", Status.BAD_REQUEST_400));

    if (!authRepository.isValidLoginToken(email, loginToken)) throw new HttpException("login-token validation failed", Status.UNAUTHORIZED_401);

    User user = userRepository.findByEmail(email).orElseThrow();
    String userAgent = request.headers().get(HeaderNames.USER_AGENT).asOptional().orElse("unknown");
    Session session = sessionRepository.createForUser(user, userAgent).orElseThrow(() -> new HttpException("unauthorized", Status.UNAUTHORIZED_401));

    response.headers().addCookie(SessionUtils.createCookie(session));
    response.send();
  }

  private void login(ServerRequest request, ServerResponse response) {
    LoginForm loginForm = request.content().as(LoginForm.class);
    Validate.fields(loginForm);

    String email = loginForm.email();
    userRepository.findByEmail(email).orElseThrow();
    UUID uuid = authRepository.generateAndGetLoginToken(email);

    logger.info("SENDING MAGIC LINK TO {}: http://localhost:5173/#/authenticate?token={}&email={}", email, uuid, email);

    response.status(Status.CREATED_201).send();
  }

  private void logout(ServerRequest request, ServerResponse response) {
    Optional<Principal> principalOption = request.context().get(Principal.class);
    Optional<String> sessionIdOption = SessionUtils.getSessionIdOption(request.headers().cookies().toMap());
    logger.debug("logging out user {} with session {}", principalOption, sessionIdOption);

    sessionIdOption.ifPresent(sessionId -> sessionRepository.deleteById(UUID.fromString(sessionId)));
    response.headers().addCookie(SessionUtils.createLogoutCookie());
    response.headers().add(HeaderNames.LOCATION, "/");
    response.status(Status.FOUND_302).send();
  }
}
