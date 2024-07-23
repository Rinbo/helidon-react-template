package io.helidon.examples.quickstart.se.utils;

import java.util.Objects;
import java.util.Set;

import io.helidon.common.context.Contexts;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public final class Validate {
  private Validate() {
    throw new IllegalStateException();
  }

  public static <T> void fields(T object) {
    Set<ConstraintViolation<T>> errors = Contexts.globalContext().get(Validator.class).orElseThrow().validate(object);
    if (errors.isEmpty()) return;

    throw new ConstraintViolationException(errors);
  }

  public static void passcode(String value) {
    Objects.requireNonNull(value, "value must not be null");
    if (!value.matches("\\d{6}")) throw new IllegalArgumentException("passcode must be a number of 6 digits");
  }
}
