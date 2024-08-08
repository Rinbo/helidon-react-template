package dev.borjessons.helidon.react.template.notify;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

/**
 * Use this component to notify all server instances to invalidate their local caches
 */
public class CacheInvalidatorNotifier {
  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

  private final ChannelNotifier channelNotifier;

  public CacheInvalidatorNotifier(ChannelNotifier channelNotifier) {
    Objects.requireNonNull(channelNotifier, "channelNotifier must not be null");

    this.channelNotifier = channelNotifier;
  }

  public void invalidateSession(UUID uuid) {
    JsonObject jsonObject = JSON.createObjectBuilder().add(ChannelReceiver.SESSION_ID_KEY, uuid.toString()).build();
    channelNotifier.notify(Channel.SESSION_CACHE, jsonObject);
  }

  public void invalidateUser(int userId) {
    JsonObject jsonObject = JSON.createObjectBuilder().add(ChannelReceiver.USER_ID_KEY, userId).build();
    channelNotifier.notify(Channel.USER_CACHE, jsonObject);
  }
}
