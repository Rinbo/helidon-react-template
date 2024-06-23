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
import io.helidon.examples.quickstart.se.service.v1.UserService;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.staticcontent.StaticContentService;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private Main() {
  }

  public static void main(String[] args) {
    LogConfig.configureRuntime();

    Config config = Config.create();
    Config.global(config);

    Config dbConfig = config.get("db");
    DbClient dbClient = DbClient.create(dbConfig);
    Contexts.globalContext().register(dbClient);

    Flyway flyway = Flyway.configure().dataSource(createDatasource(dbConfig)).load();
    flyway.migrate();

    WebServer.builder()
        .config(config.get("server"))
        .routing(Main::routing)
        .build()
        .start();
  }

  static void routing(HttpRouting.Builder routing) {
    routing
        .register("/api/v1", new UserService())
        .register("/", StaticContentService.builder("/web")
            .welcomeFileName("index.html")
            .build());
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
}