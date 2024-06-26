package io.helidon.examples.quickstart.se;

import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.dto.UserForm;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

@ServerTest
@Testcontainers
class UserServiceTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

  private final Http1Client client;

  public UserServiceTest(Http1Client client) {
    this.client = client;
  }

  @SetUpRoute
  static void routing(HttpRouting.Builder builder) {
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
    Main.configureRouting(builder);
  }

  @Test
  void testGetUsers() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    userRepository.createUser(new UserForm("robin.b@ex.com", "Robin"));

    try (Http1ClientResponse response = client.get("/api/v1/users").request()) {
      MatcherAssert.assertThat(response.status(), Matchers.is(Status.OK_200));
    }
  }
}
