package dev.borjessons.helidon.react.template.data.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasscodeTest {
  @Test
  void create() {
    Passcode passcode = Passcode.create();
    Assertions.assertNotNull(passcode);
  }
}