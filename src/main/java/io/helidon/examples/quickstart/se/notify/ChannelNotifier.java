package io.helidon.examples.quickstart.se.notify;

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

    dbClient.execute().createQuery("SELECT pg_notify(quote_ident(:channel), quote_literal(:payload))")
        .addParam("channel", channel.toString())
        .addParam("payload", jsonObject.toString())
        .execute();
  }
}
