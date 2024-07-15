package io.helidon.examples.quickstart.se.data.cleanup;

import io.helidon.common.context.Contexts;
import io.helidon.examples.quickstart.se.data.repository.AuthRepository;
import io.helidon.examples.quickstart.se.data.repository.SessionRepository;

public class DbCleanUpRunner {
  private final AuthRepository authRepository;
  private final SessionRepository sessionRepository;

  public DbCleanUpRunner(AuthRepository authRepository, SessionRepository sessionRepository) {
    this.authRepository = authRepository;
    this.sessionRepository = sessionRepository;
  }

  public DbCleanUpRunner() {
    authRepository = Contexts.globalContext().get(AuthRepository.class).orElseThrow();
    sessionRepository = Contexts.globalContext().get(SessionRepository.class).orElseThrow();
  }

  public void cleanUp() {
    authRepository.cleanUpTokens();
    sessionRepository.cleanUpSessions();
  }
}
