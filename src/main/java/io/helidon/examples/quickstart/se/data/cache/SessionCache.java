package io.helidon.examples.quickstart.se.data.cache;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;

import io.helidon.examples.quickstart.se.data.model.Session;

public class SessionCache {
  private static final Logger logger = LoggerFactory.getLogger(SessionCache.class);

  private final Cache<UUID, Session> cache;

  public SessionCache(Cache<UUID, Session> cache) {
    Objects.requireNonNull(cache, "Cache cannot be null");

    this.cache = cache;
  }

  public Session get(UUID uuid) {
    Objects.requireNonNull(uuid, "UUID cannot be null");

    logger.debug("attempting to get session from cache {}", uuid);

    return cache.getIfPresent(uuid);
  }

  // TODO when invalidating make sure to notify other instances to also invalidate the session cache
  public void invalidate(UUID uuid) {
    Objects.requireNonNull(uuid, "UUID cannot be null");

    logger.debug("invalidating cached session {}", uuid);

    cache.invalidate(uuid);
  }

  public void put(Session session) {
    logger.debug("putting session into cache {}", session);

    cache.put(session.id(), session);
  }
}
