package io.helidon.examples.quickstart.se.data.model;

import java.util.Objects;

public record LoginPasscode(Passcode passcode, String email, long expiry, short attempts) {
  public LoginPasscode {
    Objects.requireNonNull(passcode, "Passcode must not be null");
    Objects.requireNonNull(email, "Email must not be null");
    if (expiry <= 0) throw new IllegalArgumentException("expiry must be greater than zero");
    if (attempts < 0) throw new IllegalArgumentException("attempts must be greater than zero");
  }

  public static LoginPasscode of(Passcode passcode, String email, long expiry, short attempts) {
    return new LoginPasscode(passcode, email, expiry, attempts);
  }
}
