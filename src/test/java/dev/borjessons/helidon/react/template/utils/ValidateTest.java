package dev.borjessons.helidon.react.template.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidateTest {

  @Test
  void passcodeTest() {
    Assertions.assertDoesNotThrow(() -> Validate.passcode("123456"));
    Assertions.assertDoesNotThrow(() -> Validate.passcode("000000"));
    Assertions.assertDoesNotThrow(() -> Validate.passcode("999999"));

    Assertions.assertThrows(IllegalArgumentException.class, () -> Validate.passcode("123"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Validate.passcode("55555"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Validate.passcode("sixten"));
  }
}