package io.helidon.examples.quickstart.se.data.model;

import java.util.concurrent.ThreadLocalRandom;

import io.helidon.examples.quickstart.se.utils.Validate;

public record Passcode(String value) {
  private static final int CODE_LENGTH = 6;

  public Passcode {
    Validate.passcode(value);
  }

  public static Passcode create() {
    return new Passcode(generateCode());
  }

  public static Passcode of(String value) {
    return new Passcode(value);
  }

  private static String generateCode() {
    StringBuilder code = new StringBuilder();

    for (int i = 0; i < CODE_LENGTH; i++) {
      code.append(ThreadLocalRandom.current().nextInt(10));
    }

    return code.toString();
  }
}
