package io.helidon.examples.quickstart.se.notify;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Objects;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelListener implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(ChannelListener.class);

  private final Connection connection;

  public ChannelListener(Connection connection) {
    Objects.requireNonNull(connection, "connection cannot be null");
    this.connection = connection;
  }

  private static void listenToChannel(Connection connection, Channel channel) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("LISTEN " + channel.name());
    }
  }

  @Override
  public void run() {
    logger.info("starting ChannelListener");

    PGConnection pgConnection = getPgConnection();

    while (!Thread.interrupted()) {
      try {
        Thread.sleep(1000);
        doWorkOnce(pgConnection);
      } catch (InterruptedException e) {
        logger.error("", e);
        Thread.currentThread().interrupt();
      } catch (SQLException | RuntimeException e) {
        logger.error("", e);
      }
    }

    try {
      connection.close();
    } catch (SQLException e) {
      logger.error("Failed to close pg connection", e);
    }

    logger.info("exiting from channel listener");
  }

  public void startListening() {
    try {
      listenToChannel(connection, Channel.SESSION_CACHE);
      listenToChannel(connection, Channel.USER_CACHE);
    } catch (SQLException e) {
      logger.error("failed to listen to pg notifications", e);
      throw new IllegalStateException(e);
    }
  }

  private void doWorkOnce(PGConnection pgConnection) throws SQLException {
    PGNotification[] notifications = pgConnection.getNotifications();
    if (notifications != null) {
      Arrays.stream(notifications).forEach(notification -> {
        logger.info("received notification with name: {}", notification.getName());
        logger.info("received notification with parameter: {}", notification.getParameter());
      });
    }
  }

  private PGConnection getPgConnection() {
    try {
      return connection.unwrap(PGConnection.class);
    } catch (SQLException e) {
      logger.error("unable to start listener due to pg error", e);
      throw new IllegalStateException(e);
    }
  }
}
