package io.helidon.examples.quickstart.se.data.repository;

import io.helidon.dbclient.DbClient;

public class AuthRepository {
  private final DbClient dbClient;

  public AuthRepository(DbClient dbClient) {
    this.dbClient = dbClient;
  }
}
