package dev.borjessons.helidon.react.template;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import dev.borjessons.helidon.react.template.data.model.Role;
import dev.borjessons.helidon.react.template.data.repository.UserRepository;
import dev.borjessons.helidon.react.template.dto.EditUserForm;
import dev.borjessons.helidon.react.template.dto.LoginForm;
import dev.borjessons.helidon.react.template.dto.RegistrationForm;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpServer;

@ServerTest
@Testcontainers
public class AuthServiceTest {
  public static final String ROBIN = "robin.b@ex.com";
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
  protected final Http1Client client;

  public AuthServiceTest(Http1Client client) {
    this.client = client;
  }

  @BeforeAll
  static void beforeAll() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    userRepository.createUser(new RegistrationForm(ROBIN, "Robin"));
  }

  @SetUpServer
  static void setupServer(WebServerConfig.Builder builder) {
    postgres.start();

    Config config = Config.create(ConfigSources.create(Map.of(
        "db.source", "jdbc",
        "db.connection.url", postgres.getJdbcUrl(),
        "db.connection.username", postgres.getUsername(),
        "db.connection.password", postgres.getPassword(),
        "app.profile", "local"
    )), ConfigSources.file(Path.of("src/test/resources/application-test.yaml")));

    Main.setup(config);
    builder.config(config.get("server"))
        .routing(Main::configureRouting);
  }

  private static @NotNull RegistrationForm createRegForm(String email) {
    return new RegistrationForm(email, "Tester");
  }

  @Test
  void authorizationTest() throws InterruptedException {
    // Not logged in trying to get protected resource
    try (Http1ClientResponse response = client.get("/api/v1/users").request()) {
      Assertions.assertEquals(Status.UNAUTHORIZED_401, response.status());
    }

    String userCookieString = login();

    // Now logged in with role USER - should be able to get resources
    try (Http1ClientResponse response = client.get("/api/v1/users").header(HeaderNames.COOKIE, userCookieString).request()) {
      Assertions.assertEquals(Status.OK_200, response.status());
    }

    // Only role USER tries to perform ADMIN activity
    try (Http1ClientResponse response = client.post("/api/v1/users").header(HeaderNames.COOKIE, userCookieString).submit(createRegForm("sune@ex.com"))) {
      Assertions.assertEquals(Status.FORBIDDEN_403, response.status());
    }

    // Add ADMIN role to user and login
    Contexts.globalContext().get(UserRepository.class).orElseThrow().updateUser(1, new EditUserForm("Robin", List.of(Role.USER, Role.ADMIN)));
    Thread.sleep(500);
    String adminCookieString = login();

    // Now we should be able to perform admin activity
    try (Http1ClientResponse response = client.post("/api/v1/users").header(HeaderNames.COOKIE, adminCookieString).submit(createRegForm("sune@ex.com"))) {
      Assertions.assertEquals(Status.CREATED_201, response.status());
    }
  }

  @Test
  void loginAndAuthenticationTest() {
    String cookieString = login();
    Assertions.assertNotNull(cookieString);
  }

  @Test
  void registrationTest() {
    try (Http1ClientResponse response = client.post("/auth/web/register").submit(createRegForm("test@ex.com"))) {
      Assertions.assertEquals(Status.CREATED_201, response.status());
      Assertions.assertTrue(Contexts.globalContext().get(UserRepository.class).orElseThrow().findByEmail("test@ex.com").isPresent());
    }
  }

  private String authenticate(String passcode) {
    try (Http1ClientResponse authResponse = client.post("/auth/web/authenticate").queryParam("email", ROBIN).queryParam("passcode", passcode).request()) {
      Assertions.assertEquals(Status.OK_200, authResponse.status());
      Header header = authResponse.headers().get(HeaderNames.SET_COOKIE);
      Assertions.assertNotNull(header);
      Assertions.assertTrue(header.values().contains("JSESSION"));
      return header.values();
    }
  }

  private String getPasscode() {
    DbClient dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
    DbRow dbRow = dbClient.execute().get("SELECT * FROM login_passcode where email = ?", ROBIN).orElseThrow();
    return dbRow.column("passcode").getString();
  }

  private String login() {
    try (Http1ClientResponse loginResponse = client.post("/auth/web/login").submit(new LoginForm(ROBIN))) {
      Assertions.assertEquals(Status.CREATED_201, loginResponse.status());
      String passcode = getPasscode();
      return authenticate(passcode);
    }
  }
}
