package io.helidon.examples.quickstart.se.utils;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SessionUtilsTest {

  @Test
  void patternTest() {
    Map<String, List<String>> headerMap = Map.of("Cookie",
        List.of("MYSESSION=c4d0b238-ce60-4b3d-913a-577f776ba047; JSESSION=ed57c958-9ae2-44bd-8436-f11e6a0665b9"));

    Assertions.assertEquals("ed57c958-9ae2-44bd-8436-f11e6a0665b9", SessionUtils.getSessionIdOption(headerMap).orElseThrow());
    Assertions.assertTrue(SessionUtils.getSessionIdOption(Map.of()).isEmpty());
    Assertions.assertTrue(SessionUtils.getSessionIdOption(Map.of("Cookie", List.of("MYSESSION=c4d0b238-ce60-4b3d-913a-577f776ba047"))).isEmpty());
    Assertions.assertTrue(SessionUtils.getSessionIdOption(Map.of("Cookie", List.of("JSESSIONID=c4d0b238-ce60-4b3d-913a-577f776ba047"))).isEmpty());
    Assertions.assertTrue(SessionUtils.getSessionIdOption(Map.of("WrongHeaderName", List.of("JSESSION=c4d0b238-ce60-4b3d-913a-577f776ba047"))).isEmpty());
  }

}