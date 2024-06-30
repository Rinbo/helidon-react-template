package io.helidon.examples.quickstart.se;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.security.AtnProvider;
import io.helidon.examples.quickstart.se.security.AuthFilter;
import io.helidon.examples.quickstart.se.service.v1.UserService;
import io.helidon.http.Status;
import io.helidon.logging.common.LogConfig;
import io.helidon.security.Security;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.staticcontent.StaticContentService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private Main() {
    throw new IllegalStateException();
  }

  public static void main(String[] args) {
    LogConfig.configureRuntime();

    Config config = Config.create();
    setup(config);

    Security security = Security.builder()
        .config(config)
        .addProvider(new AtnProvider())
        .build();

    WebServer.builder()
        .config(config.get("server"))
        .routing(Main::configureRouting)
        .build()
        .start();
  }

  static void configureRouting(HttpRouting.Builder routing) {
    routing
        .addFilter(AuthFilter.create())
        .register("/api/v1", new UserService())
        .any("/web/register", (request, response) -> {
          logger.info("REGISTER ENDPOINT CALLED");
          response.send("hello from register");
        })
        .any("/web/authenticate", (request, response) -> {
          logger.info("AUTHENTICATE ENDPOINT CALLED");
          response.send("hello from authenticate");
        })
        .register("/", StaticContentService.builder("/web").welcomeFileName("index.html").build())
        .error(ConstraintViolationException.class, Main::handleError);
  }

  static void setup(Config config) {
    Config.global(config);

    Config dbConfig = config.get("db");
    registerDbClient(dbConfig);
    runFlywayMigration(dbConfig);

    registerValidator();
    registerRepositories();
  }

  private static DataSource createDatasource(Config dbConfig) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(dbConfig.get("connection.url").asString().get());
    hikariConfig.setUsername(dbConfig.get("connection.username").asString().get());
    hikariConfig.setPassword(dbConfig.get("connection.password").asString().get());
    hikariConfig.setMaximumPoolSize(dbConfig.get("pool.max-pool-size").asInt().get());
    hikariConfig.setMinimumIdle(dbConfig.get("pool.min-idle").asInt().get());
    hikariConfig.setConnectionTimeout(dbConfig.get("pool.connection-timeout").asLong().get());

    return new HikariDataSource(hikariConfig);
  }

  private static void handleError(ServerRequest req, ServerResponse res, ConstraintViolationException ex) {
    res.status(Status.BAD_REQUEST_400);
    res.send("Unable to parse request. Message: " + ex.getMessage());
  }

  private static void registerDbClient(Config dbConfig) {
    DbClient dbClient = DbClient.create(dbConfig);
    Contexts.globalContext().register(dbClient);
  }

  private static void registerRepositories() {
    Contexts.globalContext().register(new UserRepository());
  }

  private static void registerValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Contexts.globalContext().register(factory.getValidator());
    }
  }

  private static void runFlywayMigration(Config dbConfig) {
    Flyway flyway = Flyway.configure().dataSource(createDatasource(dbConfig)).load();
    flyway.migrate();
  }
}