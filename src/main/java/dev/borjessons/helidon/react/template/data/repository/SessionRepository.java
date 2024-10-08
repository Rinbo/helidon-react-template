package dev.borjessons.helidon.react.template.data.repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.borjessons.helidon.react.template.data.cache.SessionCache;
import dev.borjessons.helidon.react.template.data.model.Session;
import dev.borjessons.helidon.react.template.data.model.User;
import dev.borjessons.helidon.react.template.notify.CacheInvalidatorNotifier;
import dev.borjessons.helidon.react.template.utils.Constants;
import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbTransaction;

public class SessionRepository {
  private static final Logger logger = LoggerFactory.getLogger(SessionRepository.class);

  private final Clock clock;
  private final DbClient dbClient;
  private final SessionCache sessionCache;
  private final CacheInvalidatorNotifier cacheInvalidatorNotifier;

  public SessionRepository(Clock clock, DbClient dbClient, SessionCache sessionCache, CacheInvalidatorNotifier cacheInvalidatorNotifier) {
    Objects.requireNonNull(clock, "clock must not be null");
    Objects.requireNonNull(dbClient, "dbClient must not be null");
    Objects.requireNonNull(sessionCache, "sessionCache must not be null");
    Objects.requireNonNull(cacheInvalidatorNotifier, "cacheInvalidator must not be null");

    this.clock = clock;
    this.dbClient = dbClient;
    this.sessionCache = sessionCache;
    this.cacheInvalidatorNotifier = cacheInvalidatorNotifier;
  }

  public SessionRepository() {
    clock = Clock.systemUTC();
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
    sessionCache = Contexts.globalContext().get(SessionCache.class).orElseThrow();
    cacheInvalidatorNotifier = Contexts.globalContext().get(CacheInvalidatorNotifier.class).orElseThrow();
  }

  private static long executeCountByUserIdQuery(DbExecute dbExecute, int userId) {
    return dbExecute.createQuery("SELECT count(*) AS session_count FROM sessions WHERE user_id = :userId")
        .addParam("userId", userId)
        .execute()
        .map(dbRow -> dbRow.column("session_count").getLong())
        .findFirst()
        .orElse(0L);
  }

  private static long executeDeleteById(DbExecute dbExecute, UUID oldestSessionId) {
    return dbExecute.createDelete("DELETE FROM sessions WHERE id = :id")
        .addParam("id", oldestSessionId)
        .execute();
  }

  private static long executeInsertSession(DbTransaction transaction, UUID uuid, int userId, Instant expires, String userAgent) {
    return transaction.createInsert("INSERT INTO sessions (id, user_id, expires, user_agent) VALUES (:id, :userId, :expires, :userAgent)")
        .addParam("id", uuid)
        .addParam("userId", userId)
        .addParam("expires", new Date(expires.toEpochMilli()))
        .addParam("userAgent", userAgent)
        .execute();
  }

  private static Session mapToSession(DbRow row) {
    return new Session(
        UUID.fromString(row.column("id").getString()),
        row.column("user_id").getInt(),
        row.column("expires").get(ZonedDateTime.class)
    );
  }

  public void cleanUpSessions() {
    Instant startTime = clock.instant();
    long deleteCount = 0;
    DbTransaction transaction = dbClient.transaction();
    try {
      transaction.query("SELECT id FROM schedule_lock WHERE type = 'SESSIONS' FOR UPDATE SKIP LOCKED");

      transaction.createQuery("SELECT id FROM sessions WHERE expires < NOW()")
          .execute()
          .map(row -> row.column("id").getString())
          .map(UUID::fromString)
          .forEach(cacheInvalidatorNotifier::invalidateSession);

      deleteCount = dbClient.execute().createDelete("DELETE FROM sessions WHERE expires < NOW()").execute();
      transaction.commit();
    } catch (RuntimeException e) {
      logger.error("failed to cleanup sessions", e);
      transaction.rollback();
    } finally {
      logger.debug("clean up {} sessions. Time taken: {}", deleteCount, Duration.between(startTime, clock.instant()));
    }
  }

  public Optional<Session> createForUser(User user, String userAgent) {
    Objects.requireNonNull(user, "user must not be null");
    Objects.requireNonNull(userAgent, "userAgent must not be null");

    int userId = user.id();
    long insertCount = 0;
    UUID uuid = UUID.randomUUID();
    Instant expires = clock.instant().plus(Constants.SESSION_DURATION);

    DbTransaction transaction = dbClient.transaction();

    try {
      transaction.query("SELECT user_id FROM sessions WHERE user_id = ? FOR UPDATE", userId);
      cleanupOldSessions(transaction, userId);
      insertCount = executeInsertSession(transaction, uuid, userId, expires, userAgent);
      transaction.commit();

      logger.debug("session insert count {}", insertCount);
    } catch (RuntimeException e) {
      logger.error("failed to insert session", e);
      transaction.rollback();
    }

    if (insertCount > 0) {
      Session session = new Session(uuid, userId, ZonedDateTime.ofInstant(expires, ZoneOffset.UTC));
      sessionCache.put(session);
      return Optional.of(session);
    }

    logger.warn("session insert count is zero");
    return Optional.empty();
  }

  public void deleteById(UUID id) {
    long deleteCount = executeDeleteById(dbClient.execute(), id);
    logger.debug("deleted session: {}, count {}", id, deleteCount);

    cacheInvalidatorNotifier.invalidateSession(id);
  }

  public Optional<Session> findById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");

    logger.debug("looking up sessionId {}", id);

    Session session = sessionCache.get(id);

    if (session == null) {
      logger.debug("nothing in cache, looking in db");

      Optional<Session> sessionOptional = dbClient.execute()
          .createQuery("SELECT * FROM sessions WHERE id = :id")
          .addParam("id", id.toString())
          .execute()
          .map(SessionRepository::mapToSession)
          .findFirst();

      logger.debug("found session in db: {}", sessionOptional.isPresent());

      sessionOptional.ifPresent(sessionCache::put);
      return sessionOptional;
    }

    logger.debug("found session in cache {}", session);
    return Optional.of(session);
  }

  /**
   * We allow max 5 sessions per user
   */
  private void cleanupOldSessions(DbTransaction transaction, int userId) {
    long sessionCount = executeCountByUserIdQuery(transaction, userId);

    if (sessionCount >= 5) {
      transaction.createQuery("SELECT * FROM sessions WHERE user_id = :userId ORDER BY expires DESC OFFSET 4")
          .addParam("userId", userId)
          .execute()
          .map(SessionRepository::mapToSession)
          .forEach(session -> {
            UUID idToDelete = session.id();
            long deleteCount = executeDeleteById(transaction, idToDelete);
            logger.debug("cleanup job - session delete count: {}", deleteCount);
            cacheInvalidatorNotifier.invalidateSession(idToDelete);
          });
    }
  }
}
