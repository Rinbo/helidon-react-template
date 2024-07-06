package io.helidon.examples.quickstart.se.security;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.examples.quickstart.se.data.repository.AuthRepository;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.dto.LoginForm;
import io.helidon.examples.quickstart.se.utils.Validate;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class AuthService implements HttpService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final AuthRepository authRepository;

  public AuthService(UserRepository userRepository, AuthRepository authRepository) {
    Objects.requireNonNull(userRepository, "userRepository is required");
    Objects.requireNonNull(authRepository, "authRepository is required");

    this.userRepository = userRepository;
    this.authRepository = authRepository;
  }

  public AuthService() {
    Context context = Contexts.globalContext();
    userRepository = context.get(UserRepository.class).orElseThrow();
    authRepository = context.get(AuthRepository.class).orElseThrow();
  }

  @Override
  public void routing(HttpRules rules) {
    rules.post("/login", this::login);
    rules.post("/authenticate", this::authenticate);
  }

  private void authenticate(ServerRequest request, ServerResponse response) {
    /*
    Here we need a token and an email address to be sent in the body. Then I look up that token and if I find one, and it has not expired. Then I construct a cookie with a jwt.
     */

    response.send();
  }

  private void login(ServerRequest request, ServerResponse response) {
    LoginForm loginForm = request.content().as(LoginForm.class);
    Validate.fields(loginForm);

    String email = loginForm.email();
    userRepository.findByEmail(email).orElseThrow();
    UUID uuid = authRepository.generateAndGetLoginToken(email);

    logger.info("SENDING MAGIC LINK TO {}: http://localhost:5173/authenticate?token={}&email={}", email, uuid, email);

    response.status(Status.CREATED_201).send();
  }
}
