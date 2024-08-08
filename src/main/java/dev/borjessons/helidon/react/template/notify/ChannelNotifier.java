package dev.borjessons.helidon.react.template.notify;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import jakarta.json.JsonObject;

public class ChannelNotifier {
  private static final Logger logger = LoggerFactory.getLogger(ChannelNotifier.class);

  private final DbClient dbClient;

  public ChannelNotifier() {
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
  }

  public void notify(Channel channel, JsonObject jsonObject) {
    logger.debug("notifying {}", channel);

    try (Connection connection = dbClient.unwrap(Connection.class);
        Statement statement = connection.createStatement()) {
      statement.execute(String.format(Locale.ROOT, "NOTIFY %s, '%s'", channel.toString(), jsonObject.toString()));
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
}
