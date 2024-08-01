package io.helidon.examples.quickstart.se.data.cache;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;

import io.helidon.examples.quickstart.se.data.model.User;

public class UserCache {
  private static final Logger logger = LoggerFactory.getLogger(UserCache.class);

  private final Cache<Integer, User> cache;

  public UserCache(Cache<Integer, User> cache) {
    Objects.requireNonNull(cache, "Cache cannot be null");

    this.cache = cache;
  }

  public User get(int userId) {
    logger.debug("attempting to get user from cache, userId: {}", userId);

    return cache.getIfPresent(userId);
  }

  public void invalidate(int userId) {
    logger.debug("invalidating cached user {}", userId);

    cache.invalidate(userId);
  }

  public void put(User user) {
    logger.debug("putting user into cache {}", user);

    cache.put(user.id(), user);
  }
}
