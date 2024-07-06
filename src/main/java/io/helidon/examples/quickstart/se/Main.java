package io.helidon.examples.quickstart.se;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.helidon.common.context.Context;
import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.examples.quickstart.se.data.cache.SessionCache;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.examples.quickstart.se.data.repository.AuthRepository;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.security.AuthFilter;
import io.helidon.examples.quickstart.se.security.AuthService;
import io.helidon.examples.quickstart.se.service.v1.UserService;
import io.helidon.http.Status;
import io.helidon.logging.common.LogConfig;
import io.helidon.scheduling.Scheduling;
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
        .register("/api", new AuthService()) // TODO figure out a better name space
        .register("/", StaticContentService.builder("/web").welcomeFileName("index.html").build())
        .error(ConstraintViolationException.class, Main::handleBadArgument)
        .error(NoSuchElementException.class, Main::handleNotFound);
  }

  static void setup(Config config) {
    Config.global(config);

    Config dbConfig = config.get("db");
    registerDbClient(dbConfig);
    runFlywayMigration(dbConfig);

    registerValidator();

    AuthRepository authRepository = new AuthRepository();
    registerRepositories(authRepository);
    registerCaches();
    configureScheduledJobs(authRepository);
  }

  private static void configureScheduledJobs(AuthRepository authRepository) {
    Scheduling.fixedRate()
        .delay(10)
        .initialDelay(5)
        .timeUnit(TimeUnit.MINUTES)
        .task(invocation -> authRepository.cleanUpTokens())
        .build();
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

  private static void handleBadArgument(ServerRequest req, ServerResponse res, ConstraintViolationException ex) {
    res.status(Status.BAD_REQUEST_400);
    res.send("Unable to parse request. Message: " + ex.getMessage());
  }

  private static void handleNotFound(ServerRequest req, ServerResponse res, NoSuchElementException ex) {
    res.status(Status.NOT_FOUND_404);
    res.send("No such element: " + ex.getMessage());
  }

  private static void registerCaches() {
    Cache<UUID, Session> sessionCache = Caffeine.newBuilder()
        .initialCapacity(1000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .build();

    Context context = Contexts.globalContext();
    context.register(new SessionCache(sessionCache));
  }

  private static void registerDbClient(Config dbConfig) {
    DbClient dbClient = DbClient.create(dbConfig);
    Contexts.globalContext().register(dbClient);
  }

  private static void registerRepositories(AuthRepository authRepository) {
    Context context = Contexts.globalContext();
    context.register(authRepository);
    context.register(new UserRepository());
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