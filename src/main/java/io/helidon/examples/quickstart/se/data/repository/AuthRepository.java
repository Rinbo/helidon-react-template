package io.helidon.examples.quickstart.se.data.repository;

import java.util.List;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.examples.quickstart.se.dto.LoginForm;

public class AuthRepository {
  private final DbClient dbClient;

  public AuthRepository() {
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
  }

  public void generateLoginToken(LoginForm loginForm) {
    List<String> hello = List.of("hello");

  }
}
