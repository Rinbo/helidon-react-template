package io.helidon.examples.quickstart.se.notify;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.dbclient.DbClient;

public class ChannelNotifier {
  private static final Logger logger = LoggerFactory.getLogger(ChannelNotifier.class);

  private final DbClient dbClient;

  public ChannelNotifier(DbClient dbClient) {
    Objects.requireNonNull(dbClient, "dbClient must not be null");
    this.dbClient = dbClient;
  }

  public void notify(Channel channel) {
    logger.debug("notifying {}", channel);

    try (Statement statement = dbClient.unwrap(Connection.class).createStatement()) {
      switch (channel) {
        case USER_CACHE -> statement.execute("NOTIFY user_cache, 'invalidate'");
        case SESSION_CACHE -> statement.execute("NOTIFY session_cache, 'invalidate'");
      }
      ;

    } catch (SQLException e) {
      logger.error("failed to notify", e);
      throw new IllegalStateException(e);
    }
  }
}
