package io.helidon.examples.quickstart.se.data.repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbTransaction;
import io.helidon.examples.quickstart.se.data.model.Passcode;

public class AuthRepository {
  private static final long LOGIN_PASSCODE_VALIDITY_PERIOD = 1000 * 60 * 5;
  private static final Logger logger = LoggerFactory.getLogger(AuthRepository.class);

  private final DbClient dbClient;
  private final Clock clock;

  public AuthRepository(DbClient dbClient, Clock clock) {
    Objects.requireNonNull(dbClient, "dbClient must not be null");
    Objects.requireNonNull(dbClient, "clock must not be null");

    this.dbClient = dbClient;
    this.clock = clock;
  }

  public AuthRepository() {
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
    clock = Clock.systemUTC();
  }

  public void cleanUpTokens() {
    Instant now = clock.instant();
    long rowCount = 0;

    try {
      DbTransaction transaction = dbClient.transaction();
      rowCount = transaction
          .createDelete("DELETE FROM login_passcode WHERE expiry < :now")
          .addParam("now", now.toEpochMilli())
          .execute();
      transaction.commit();
    } catch (RuntimeException e) {
      logger.error("Failed to clean up login tokens", e);
    } finally {
      logger.debug("Cleaned up {} tokens. Time taken: {}", rowCount, Duration.between(clock.instant(), now));
    }
  }

  public Passcode generateAndGetLoginPasscode(String email) {
    Objects.requireNonNull(email, "email must not be null");

    DbTransaction transaction = dbClient.transaction();

    transaction.createDelete("DELETE FROM login_passcode WHERE email = :email")
        .addParam("email", email)
        .execute();

    Passcode passcode = Passcode.create();

    long updateCount = transaction.createInsert("INSERT INTO login_passcode (passcode, email, expiry) VALUES (:passcode, :email, :expiry)")
        .addParam("passcode", passcode.value())
        .addParam("email", email)
        .addParam("expiry", clock.instant().toEpochMilli() + LOGIN_PASSCODE_VALIDITY_PERIOD)
        .execute();

    transaction.commit();

    if (updateCount != 1) throw new IllegalStateException("passcode generation failed for email: " + email);

    return passcode;
  }

  public boolean isValidLoginPasscode(String email, Passcode passcode) {
    OptionalLong expiryOption = dbClient.execute()
        .createQuery("SELECT * FROM login_passcode WHERE email = :email AND passcode = :passcode")
        .addParam("email", email)
        .addParam("passcode", passcode.value())
        .execute()
        .mapToLong(dbRow -> dbRow.column("expiry").getLong())
        .findFirst();

    if (expiryOption.isEmpty()) return false;

    return expiryOption.getAsLong() > clock.instant().toEpochMilli();
  }
}
