package io.helidon.examples.quickstart.se.utils;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EitherTest {
  @Test
  void testLeft() {
    Either<String, Instant> eitherLeft = Either.left("Left");

    Assertions.assertTrue(eitherLeft.isLeft());
    Assertions.assertFalse(eitherLeft.isRight());
    Assertions.assertEquals("Left", eitherLeft.getLeft());
    Assertions.assertNull(eitherLeft.getRight());
  }

  @Test
  void testRight() {
    Either<String, Instant> eitherRight = Either.right(Instant.ofEpochMilli(0));

    Assertions.assertFalse(eitherRight.isLeft());
    Assertions.assertTrue(eitherRight.isRight());
    Assertions.assertEquals(Instant.ofEpochMilli(0), eitherRight.getRight());
    Assertions.assertNull(eitherRight.getLeft());
  }
}
