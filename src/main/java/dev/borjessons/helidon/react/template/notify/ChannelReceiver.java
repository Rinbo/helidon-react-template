package dev.borjessons.helidon.react.template.notify;

import java.util.UUID;

import io.helidon.common.context.Contexts;
import dev.borjessons.helidon.react.template.data.cache.SessionCache;
import dev.borjessons.helidon.react.template.data.cache.UserCache;
import jakarta.json.JsonObject;

public class ChannelReceiver {
  public static final String SESSION_ID_KEY = "uuid";
  public static final String USER_ID_KEY = "userId";

  private final SessionCache sessionCache;
  private final UserCache userCache;

  public ChannelReceiver() {
    this.sessionCache = Contexts.globalContext().get(SessionCache.class).orElseThrow();
    this.userCache = Contexts.globalContext().get(UserCache.class).orElseThrow();
  }

  public void receive(Channel channel, JsonObject jsonObject) {
    switch (channel) {
      case USER_CACHE -> userCache.invalidate(jsonObject.getInt(USER_ID_KEY));
      case SESSION_CACHE -> sessionCache.invalidate(UUID.fromString(jsonObject.getString(SESSION_ID_KEY)));
    }
  }
}
