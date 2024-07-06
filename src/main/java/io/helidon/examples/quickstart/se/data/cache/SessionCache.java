package io.helidon.examples.quickstart.se.data.cache;

import java.util.Objects;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;

import io.helidon.examples.quickstart.se.data.model.Session;

public class SessionCache {
  private final Cache<UUID, Session> cache;

  public SessionCache(Cache<UUID, Session> cache) {
    Objects.requireNonNull(cache, "Cache cannot be null");

    this.cache = cache;
  }

  public void invalidate(UUID uuid) {
    Objects.requireNonNull(uuid, "UUID cannot be null");

    cache.invalidate(uuid);
  }

  public void put(Session session) {
    cache.put(session.id(), session);
  }
}
