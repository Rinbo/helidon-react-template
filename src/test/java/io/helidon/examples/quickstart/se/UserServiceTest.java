package io.helidon.examples.quickstart.se;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.dto.UserForm;
import io.helidon.examples.quickstart.se.utils.TestUtils;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpServer;
import jakarta.json.JsonArray;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

@ServerTest
@Testcontainers
class UserServiceTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

  private final Http1Client client;
  private final Jsonb jsonb = JsonbBuilder.create();

  public UserServiceTest(Http1Client client) {
    this.client = client;
  }

  @BeforeAll
  static void beforeAll() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    userRepository.createUser(new UserForm("robin.b@ex.com", "Robin"));
  }

  @SetUpServer
  static void setupServer(WebServerConfig.Builder builder) {
    postgres.start();

    Config config = Config.create(ConfigSources.create(Map.of(
        "db.source", "jdbc",
        "db.connection.url", postgres.getJdbcUrl(),
        "db.connection.username", postgres.getUsername(),
        "db.connection.password", postgres.getPassword(),
        "db.pool.connection-timeout", "3000",
        "db.pool.max-pool-size", "10",
        "db.pool.min-idle", "2"
    )));

    Main.setup(config);
    builder.config(config)
        .routing(Main::configureRouting);
  }

  private static void verifyUser(User user) {
    MatcherAssert.assertThat(user.name(), Matchers.is("Robin"));
    MatcherAssert.assertThat(user.email(), Matchers.is("robin.b@ex.com"));
    MatcherAssert.assertThat(user.roles().getFirst().name(), Matchers.is("USER"));
  }

  // TODO expose in service
  @Test
  void getUserByEmail() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    Optional<User> userOption = userRepository.findByEmail("robin.b@ex.com");

    MatcherAssert.assertThat(userOption.isPresent(), Matchers.is(true));

    verifyUser(userOption.get());
  }

  // TODO expose in service
  @Test
  void getUserById() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    Optional<User> userOption = userRepository.findById(1);

    MatcherAssert.assertThat(userOption.isPresent(), Matchers.is(true));

    verifyUser(userOption.get());
  }

  @Test
  void testGetUsers() {
    try (Http1ClientResponse response = client.get("/api/v1/users").request()) {
      MatcherAssert.assertThat(response.status(), Matchers.is(Status.OK_200));
      List<User> users = TestUtils.fromJsonList(response.as(JsonArray.class), User.class);
      verifyUser(users.getFirst());
    }
  }
}
