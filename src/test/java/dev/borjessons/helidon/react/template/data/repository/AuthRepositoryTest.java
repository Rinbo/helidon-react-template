package dev.borjessons.helidon.react.template.data.repository;

import java.time.Clock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.borjessons.helidon.react.template.ServerTestBase;
import dev.borjessons.helidon.react.template.data.model.LoginPasscode;
import dev.borjessons.helidon.react.template.data.model.Passcode;
import dev.borjessons.helidon.react.template.utils.Either;
import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.webclient.http1.Http1Client;

class AuthRepositoryTest extends ServerTestBase {
  public static final String EMAIL = "dummy";
  public static final long FAR_IN_THE_FUTURE = 100000000000000L;
  public static final Passcode PASSCODE = Passcode.of("000000");
  public static final long WAY_BACK_IN_THE_PAST = 1L;
  private final DbClient dbClient;

  public AuthRepositoryTest(Http1Client client) {
    super(client);
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
  }

  private static void insertLoginPasscode(DbClient dbClient, long expiry, int attempts) {
    String sql = "INSERT INTO login_passcode(passcode, email, expiry, attempts) VALUES (?, ?, ?, ?)";
    dbClient.execute().insert(sql, "000000", "dummy", expiry, attempts);
  }

  @AfterEach
  void tearDown() {
    dbClient.execute().delete("DELETE FROM login_passcode WHERE email = ?", EMAIL);
  }

  @Test
  void validatePasscodeExpiredTest() {
    insertLoginPasscode(dbClient, WAY_BACK_IN_THE_PAST, 0);
    AuthRepository authRepository = new AuthRepository(dbClient, Clock.systemUTC());

    Either<IllegalStateException, LoginPasscode> either = authRepository.validatePasscode(EMAIL, PASSCODE);
    Assertions.assertTrue(either.isLeft());

    IllegalStateException exception = either.getLeft();
    Assertions.assertEquals("Passcode has expired", exception.getMessage());
  }

  @Test
  void validatePasscodeMaxAttemptsReachedTest() {
    insertLoginPasscode(dbClient, FAR_IN_THE_FUTURE, 10);
    AuthRepository authRepository = new AuthRepository(dbClient, Clock.systemUTC());

    Either<IllegalStateException, LoginPasscode> either = authRepository.validatePasscode(EMAIL, PASSCODE);
    Assertions.assertTrue(either.isLeft());

    IllegalStateException exception = either.getLeft();
    Assertions.assertEquals("Max attempts has been reached", exception.getMessage());
  }

  @Test
  void validatePasscodeNoActivePasscodesTest() {
    insertLoginPasscode(dbClient, FAR_IN_THE_FUTURE, 0);
    AuthRepository authRepository = new AuthRepository(dbClient, Clock.systemUTC());

    Either<IllegalStateException, LoginPasscode> either = authRepository.validatePasscode("another_email", PASSCODE);
    Assertions.assertTrue(either.isLeft());

    IllegalStateException exception = either.getLeft();
    Assertions.assertEquals("User has no active login passcodes. Please request a new one", exception.getMessage());
  }

  @Test
  void validatePasscodeSuccessTest() {
    insertLoginPasscode(dbClient, FAR_IN_THE_FUTURE, 0);
    AuthRepository authRepository = new AuthRepository(dbClient, Clock.systemUTC());

    Either<IllegalStateException, LoginPasscode> either = authRepository.validatePasscode(EMAIL, PASSCODE);
    Assertions.assertTrue(either.isRight());

    LoginPasscode loginPasscode = either.getRight();
    Assertions.assertEquals(PASSCODE, loginPasscode.passcode());
    Assertions.assertEquals(EMAIL, loginPasscode.email());
  }

  @Test
  void validatePasscodeWrongPasscodeTest() {
    insertLoginPasscode(dbClient, FAR_IN_THE_FUTURE, 0);
    AuthRepository authRepository = new AuthRepository(dbClient, Clock.systemUTC());

    Either<IllegalStateException, LoginPasscode> either = authRepository.validatePasscode(EMAIL, Passcode.of("111111"));
    Assertions.assertTrue(either.isLeft());

    IllegalStateException exception = either.getLeft();
    Assertions.assertEquals("Passcode does not match", exception.getMessage());
  }
}