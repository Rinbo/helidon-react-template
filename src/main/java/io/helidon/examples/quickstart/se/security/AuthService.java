package io.helidon.examples.quickstart.se.security;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.examples.quickstart.se.data.model.LoginPasscode;
import io.helidon.examples.quickstart.se.data.model.Passcode;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.data.repository.AuthRepository;
import io.helidon.examples.quickstart.se.data.repository.SessionRepository;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.dto.LoginForm;
import io.helidon.examples.quickstart.se.dto.RegistrationForm;
import io.helidon.examples.quickstart.se.email.EmailSender;
import io.helidon.examples.quickstart.se.utils.Either;
import io.helidon.examples.quickstart.se.utils.SessionUtils;
import io.helidon.examples.quickstart.se.utils.Validate;
import io.helidon.http.HeaderNames;
import io.helidon.http.HttpException;
import io.helidon.http.Status;
import io.helidon.security.Principal;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class AuthService implements HttpService {
  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final AuthRepository authRepository;
  private final SessionRepository sessionRepository;
  private final UserRepository userRepository;
  private final EmailSender emailSender;

  public AuthService() {
    Context context = Contexts.globalContext();
    authRepository = context.get(AuthRepository.class).orElseThrow();
    sessionRepository = context.get(SessionRepository.class).orElseThrow();
    userRepository = context.get(UserRepository.class).orElseThrow();
    emailSender = new EmailSender();
  }

  private static List<String> getCookiesStrings(ServerRequest request) {
    try {
      return request.headers().get(HeaderNames.COOKIE).allValues();
    } catch (NoSuchElementException e) {
      return List.of();
    }
  }

  @Override
  public void routing(HttpRules rules) {
    rules.post("/login", this::login);
    rules.post("/authenticate", this::authenticate);
    rules.post("/logout", this::logout);
    rules.get("/principal", this::fetchPrincipal);
    rules.post("/register", this::register);
  }

  private void authenticate(ServerRequest request, ServerResponse response) {
    String email = request.prologue().query().first("email").orElseThrow(() -> new HttpException("email missing", Status.BAD_REQUEST_400));
    String passcode = request.prologue().query().first("passcode").orElseThrow(() -> new HttpException("passcode missing", Status.BAD_REQUEST_400));

    Either<IllegalStateException, LoginPasscode> either = authRepository.validatePasscode(email, Passcode.of(passcode));

    if (either.isLeft()) throw new HttpException(either.getLeft().getMessage(), Status.UNAUTHORIZED_401);

    User user = userRepository.findByEmail(email).orElseThrow();
    String userAgent = request.headers().get(HeaderNames.USER_AGENT).asOptional().orElse("unknown");
    Session session = sessionRepository.createForUser(user, userAgent).orElseThrow(() -> new HttpException("unauthorized", Status.UNAUTHORIZED_401));

    response.headers().addCookie(SessionUtils.createCookie(session));
    response.headers().contentType(MediaTypes.APPLICATION_JSON);
    response.send(user);
  }

  /**
   * Unauthenticated route since we don't want ugly errors in frontend if user is not logged in
   */
  private void fetchPrincipal(ServerRequest request, ServerResponse response) {
    SessionUtils.getSessionIdOption(getCookiesStrings(request))
        .map(UUID::fromString)
        .flatMap(sessionRepository::findById)
        .map(Session::userId)
        .flatMap(userRepository::findById)
        .ifPresentOrElse(response::send, response::send);
  }

  private void generateAndSendMagicLink(String email) {
    Passcode passcode = authRepository.generateAndGetLoginPasscode(email);
    emailSender.sendEmail(email, passcode);

    logger.info("SENDING PASSCODE {} TO {}: ", passcode, email);
  }

  private void login(ServerRequest request, ServerResponse response) {
    LoginForm loginForm = request.content().as(LoginForm.class);
    Validate.fields(loginForm);

    String email = loginForm.email();
    userRepository.findByEmail(email).orElseThrow();
    generateAndSendMagicLink(email);

    response.status(Status.CREATED_201).send();
  }

  private void logout(ServerRequest request, ServerResponse response) {
    Optional<Principal> principalOption = request.context().get(Principal.class);
    Optional<String> sessionIdOption = SessionUtils.getSessionIdOption(getCookiesStrings(request));

    logger.debug("logging out user {} with session {}", principalOption, sessionIdOption);

    sessionIdOption.ifPresent(sessionId -> sessionRepository.deleteById(UUID.fromString(sessionId)));
    response.headers().addCookie(SessionUtils.createLogoutCookie());
    response.headers().add(HeaderNames.LOCATION, "/");
    response.status(Status.FOUND_302).send();
  }

  private void register(ServerRequest request, ServerResponse response) {
    RegistrationForm registrationForm = request.content().as(RegistrationForm.class);
    Validate.fields(registrationForm);

    userRepository.createUser(registrationForm);
    generateAndSendMagicLink(registrationForm.email());
    response.status(Status.CREATED_201).send();
  }
}
