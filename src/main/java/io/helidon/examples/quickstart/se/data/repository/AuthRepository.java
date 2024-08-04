package io.helidon.examples.quickstart.se.data.repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbTransaction;
import io.helidon.examples.quickstart.se.data.model.LoginPasscode;
import io.helidon.examples.quickstart.se.data.model.Passcode;
import io.helidon.examples.quickstart.se.utils.Constants;
import io.helidon.examples.quickstart.se.utils.Either;

public class AuthRepository {
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

  private static LoginPasscode mapToLoginPasscode(DbRow dbRow) {
    return LoginPasscode.of(
        Passcode.of(dbRow.column("passcode").getString()),
        dbRow.column("email").getString(),
        dbRow.column("expiry").getLong(),
        (short) dbRow.column("attempts").getInt()
    );
  }

  public void cleanUpTokens() {
    Instant now = clock.instant();
    long rowCount = 0;
    DbTransaction transaction = dbClient.transaction();

    try {
      transaction.query("SELECT id FROM schedule_lock WHERE type = 'PASSCODES' FOR UPDATE SKIP LOCKED");

      rowCount = transaction
          .createDelete("DELETE FROM login_passcode WHERE expiry < :now")
          .addParam("now", now.toEpochMilli())
          .execute();
      transaction.commit();
    } catch (RuntimeException e) {
      transaction.rollback();
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
        .addParam("expiry", clock.instant().toEpochMilli() + Constants.LOGIN_PASSCODE_VALIDITY_PERIOD.toMillis())
        .execute();

    transaction.commit();

    if (updateCount != 1) throw new IllegalStateException("passcode generation failed for email: " + email);

    return passcode;
  }

  public Either<IllegalStateException, LoginPasscode> validatePasscode(String email, Passcode passcode) {
    DbTransaction transaction = dbClient.transaction();

    try {
      Optional<LoginPasscode> optional = transaction
          .createQuery("SELECT * FROM login_passcode WHERE email = :email FOR UPDATE")
          .addParam("email", email)
          .execute()
          .map(AuthRepository::mapToLoginPasscode)
          .findFirst();

      transaction.update("UPDATE login_passcode SET attempts = attempts + 1 WHERE email = ?", email);
      transaction.update("UPDATE users SET last_login = now() WHERE email = ?", email);
      transaction.commit();

      if (optional.isEmpty()) return Either.left(new IllegalStateException("User has no active login passcodes. Please request a new one"));

      LoginPasscode loginPasscode = optional.get();
      if (loginPasscode.attempts() > Constants.MAX_LOGIN_ATTEMPTS - 1) return Either.left(new IllegalStateException("Max attempts has been reached"));
      if (loginPasscode.expiry() < clock.instant().toEpochMilli()) return Either.left(new IllegalStateException("Passcode has expired"));
      if (!loginPasscode.passcode().equals(passcode)) return Either.left(new IllegalStateException("Passcode does not match"));

      return Either.right(loginPasscode);
    } catch (RuntimeException e) {
      logger.error("Failed to validate login passcode", e);
      transaction.rollback();
      return Either.left(new IllegalStateException("Failed to validate login passcode"));
    }
  }
}
