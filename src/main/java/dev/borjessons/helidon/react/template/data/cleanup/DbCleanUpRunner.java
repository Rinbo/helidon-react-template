package dev.borjessons.helidon.react.template.data.cleanup;

import io.helidon.common.context.Contexts;
import dev.borjessons.helidon.react.template.data.repository.AuthRepository;
import dev.borjessons.helidon.react.template.data.repository.SessionRepository;

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
