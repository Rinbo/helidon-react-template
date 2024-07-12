package io.helidon.examples.quickstart.se.security;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AtnProviderTest {
  private static @NotNull Optional<String> getSessionIdOption(List<String> stringList) {
    return stringList.stream()
        .map(AtnProvider.SESSION_PATTERN::matcher)
        .filter(Matcher::find)
        .map(matcher -> matcher.group(1))
        .findFirst();
  }

  @Test
  void patternTest() {
    List<String> stringList = List.of("MYSESSION=\"c4d0b238-ce60-4b3d-913a-577f776ba047\"; JSESSION=ed57c958-9ae2-44bd-8436-f11e6a0665b9");

    Assertions.assertEquals("ed57c958-9ae2-44bd-8436-f11e6a0665b9", getSessionIdOption(stringList).orElseThrow());
    Assertions.assertTrue(getSessionIdOption(List.of()).isEmpty());
    Assertions.assertTrue(getSessionIdOption(List.of("MYSESSION=c4d0b238-ce60-4b3d-913a-577f776ba047")).isEmpty());
    Assertions.assertTrue(getSessionIdOption(List.of("JSESSIONID=c4d0b238-ce60-4b3d-913a-577f776ba047")).isEmpty());
  }
}