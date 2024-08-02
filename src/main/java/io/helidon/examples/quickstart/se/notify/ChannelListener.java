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

import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class ChannelListener implements Runnable {
  private static final Jsonb JSONB = JsonbBuilder.create();
  private static final Logger logger = LoggerFactory.getLogger(ChannelListener.class);

  private final Connection connection;
  private final ChannelReceiver channelReceiver;

  public ChannelListener(Connection connection, ChannelReceiver channelReceiver) {
    Objects.requireNonNull(connection, "connection cannot be null");
    Objects.requireNonNull(channelReceiver, "channelReceiver cannot be null");

    this.channelReceiver = channelReceiver;
    this.connection = connection;
  }

  private static void listenToChannel(Connection connection, Channel channel) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("LISTEN " + channel.name());
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String stripSingleQuotes(String string) {
    return string.substring(1, string.length() - 1);
  }

  @Override
  public void run() {
    logger.info("starting ChannelListener");

    PGConnection pgConnection = getPgConnection();
    startListening();

    while (!Thread.currentThread().isInterrupted()) {
      try {
        doWorkOnce(pgConnection);
      } catch (SQLException | RuntimeException e) {
        logger.error("", e);
        Thread.currentThread().interrupt();
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
    PGNotification[] notifications = pgConnection.getNotifications(1000);
    if (notifications != null) {
      Arrays.stream(notifications).forEach(notification -> {

        logger.debug("received notification with name: {}", notification.getName());
        logger.debug("received notification with parameter: {}", notification.getParameter());

        Channel.fromString(notification.getName())
            .ifPresent(channel -> channelReceiver.receive(channel, JSONB.fromJson(notification.getParameter(), JsonObject.class)));
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
