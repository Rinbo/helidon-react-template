package io.helidon.examples.quickstart.se;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpServer;

@ServerTest
@Testcontainers
public abstract class ServerTestBase {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
  protected final Http1Client client;

  public ServerTestBase(Http1Client client) {
    this.client = client;
  }

  @SetUpServer
  static void setupServer(WebServerConfig.Builder builder) {
    postgres.start();

    Config config = Config.create(ConfigSources.create(Map.of(
        "db.source", "jdbc",
        "db.connection.url", postgres.getJdbcUrl(),
        "db.connection.username", postgres.getUsername(),
        "db.connection.password", postgres.getPassword()
    )));

    Main.setup(config);
    builder.config(config)
        .routing(Main::configureRouting);
  }
}
