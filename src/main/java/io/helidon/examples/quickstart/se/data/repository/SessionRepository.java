package io.helidon.examples.quickstart.se.data.repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
  private static final Duration EXPIRY = Duration.ofMinutes(1);
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

  private static Stream<DbRow> executeCountByUserIdQuery(DbExecute dbExecute, int userId) {
    return dbExecute.createQuery("SELECT count(*) FROM sessions WHERE user_id = :userId")
        .addParam("userId", userId)
        .execute();
  }

  private static long executeDeleteById(DbExecute dbExecute, UUID oldestSessionId) {
    return dbExecute.createDelete("DELETE FROM sessions WHERE id = :id")
        .addParam("id", oldestSessionId) // Will UUID type work? Need to use string instead?
        .execute();
  }

  private static Stream<DbRow> executeFindOldestByIdQuery(DbExecute dbExecute, int userId) {
    return dbExecute.createQuery("SELECT * FROM sessions WHERE user_id = :userId ORDER BY expires ASC LIMIT 1")
        .addParam("userId", userId)
        .execute();
  }

  private static long executeInsertSession(DbTransaction transaction, UUID uuid, int userId, Instant expires) {
    return transaction.createInsert("INSERT INTO sessions (id, user_id, expires) VALUES (:id, :userId, :expires)")
        .addParam("id", uuid)
        .addParam("userId", userId)
        .addParam("expires", expires)
        .execute();
  }

  private static Session mapToSession(DbRow row) {
    return new Session(
        row.column("id").get(UUID.class),
        row.column("userId").getInt(),
        row.column("expires").get(ZonedDateTime.class)
    );
  }

  public Optional<Session> createForUser(User user) {
    Objects.requireNonNull(user, "user must not be null");

    int userId = user.id();

    DbTransaction transaction = dbClient.transaction();

    List<DbRow> sessionRows = executeCountByUserIdQuery(transaction, userId).toList();

    if (sessionRows.size() >= 5) {
      UUID oldestSessionId = executeFindOldestByIdQuery(transaction, userId)
          .map(SessionRepository::mapToSession)
          .toList()
          .getFirst()
          .id();

      long deleteCount = executeDeleteById(transaction, oldestSessionId);
      logger.debug("session delete count: {}", deleteCount);

      sessionCache.invalidate(oldestSessionId);
    }

    UUID uuid = UUID.randomUUID();
    Instant expires = clock.instant().plus(EXPIRY);
    long insertCount = executeInsertSession(transaction, uuid, userId, expires);

    logger.debug("session insert count {}", insertCount);

    transaction.commit();

    if (insertCount > 0) {
      Session session = new Session(uuid, userId, ZonedDateTime.from(expires));
      sessionCache.put(session);
      return Optional.of(session);
    }

    logger.warn("session insert count is zero");
    return Optional.empty();
  }

}
