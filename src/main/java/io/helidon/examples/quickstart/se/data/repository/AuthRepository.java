package io.helidon.examples.quickstart.se.data.repository;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbTransaction;

public class AuthRepository {
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

  public UUID generateAndGetLoginToken(String email) {
    Objects.requireNonNull(email, "email must not be null");
    
    DbTransaction transaction = dbClient.transaction();

    transaction.createDelete("DELETE FROM login_token WHERE email = :email")
        .addParam("email", email)
        .execute();

    UUID uuid = UUID.randomUUID();
    long updateCount = transaction.createInsert("INSERT INTO login_token (token, email, expiry) VALUES (:token, :email, :expiry)")
        .addParam("token", uuid.toString())
        .addParam("email", email)
        .addParam("expiry", clock.instant().toEpochMilli())
        .execute();

    transaction.commit();

    if (updateCount != 1) throw new IllegalStateException("token generation failed for email: " + email);

    return uuid;
  }
}
