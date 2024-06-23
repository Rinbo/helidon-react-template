package io.helidon.examples.quickstart.se.utils;

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
}
