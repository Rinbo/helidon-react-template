package io.helidon.examples.quickstart.se.data.repository;

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

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbTransaction;
import io.helidon.examples.quickstart.se.data.cache.SessionCache;
import io.helidon.examples.quickstart.se.data.model.Session;
import io.helidon.examples.quickstart.se.data.model.User;

public class SessionRepository {
  private static final Duration EXPIRY = Duration.ofDays(30);
  private static final Logger logger = LoggerFactory.getLogger(SessionRepository.class);

  private final Clock clock;
  private final DbClient dbClient;
  private final SessionCache sessionCache;

  public SessionRepository(Clock clock, DbClient dbClient, SessionCache sessionCache) {
    Objects.requireNonNull(clock, "clock must not be null");
    Objects.requireNonNull(dbClient, "dbClient must not be null");
    Objects.requireNonNull(sessionCache, "sessionCache must not be null");

    this.clock = clock;
    this.dbClient = dbClient;
    this.sessionCache = sessionCache;
  }

  public SessionRepository() {
    clock = Clock.systemUTC();
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
    sessionCache = Contexts.globalContext().get(SessionCache.class).orElseThrow();
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

  public Optional<Session> createForUser(User user, String userAgent) {
    Objects.requireNonNull(user, "user must not be null");
    Objects.requireNonNull(userAgent, "userAgent must not be null");

    int userId = user.id();

    DbTransaction transaction = dbClient.transaction();
    transaction.query("SELECT user_id FROM sessions WHERE user_id = ? FOR UPDATE", userId);

    cleanupOldSessions(transaction, userId);

    UUID uuid = UUID.randomUUID();
    Instant expires = clock.instant().plus(EXPIRY);
    long insertCount = executeInsertSession(transaction, uuid, userId, expires, userAgent);

    logger.debug("session insert count {}", insertCount);

    transaction.commit();

    if (insertCount > 0) {
      Session session = new Session(uuid, userId, ZonedDateTime.ofInstant(expires, ZoneOffset.UTC));
      sessionCache.put(session);
      return Optional.of(session);
    }

    logger.warn("session insert count is zero");
    return Optional.empty();
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
            logger.debug("session delete count: {}", deleteCount);
            sessionCache.invalidate(idToDelete);
          });
    }
  }
}
