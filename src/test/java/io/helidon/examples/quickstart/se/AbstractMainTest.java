package io.helidon.examples.quickstart.se;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.helidon.examples.quickstart.se.service.v1.UserService;
import io.helidon.http.Status;
import io.helidon.webclient.api.ClientResponseTyped;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.staticcontent.StaticContentService;
import io.helidon.webserver.testing.junit5.SetUpRoute;
import jakarta.json.JsonObject;

abstract class AbstractMainTest {
  private final Http1Client client;

  protected AbstractMainTest(Http1Client client) {
    this.client = client;
  }

  @SetUpRoute
  static void routing(HttpRouting.Builder builder) {
    builder
        .register("/api/v1", new UserService())
        .register("/", StaticContentService.builder("/web")
            .welcomeFileName("index.html")
            .build());
  }

  @Test
  void testGreeting() {
    ClientResponseTyped<JsonObject> response = client.get("/greet").request(JsonObject.class);
    assertThat(response.status(), is(Status.OK_200));
    assertThat(response.entity().getString("message"), is("Hello World!"));
  }

  @Test
  void testMetricsObserver() {
    try (Http1ClientResponse response = client.get("/observe/metrics").request()) {
      assertThat(response.status(), is(Status.OK_200));
    }
  }

  @Test
  void testSimpleGreet() {
    ClientResponseTyped<String> response = client.get("/simple-greet").request(String.class);
    assertThat(response.status(), is(Status.OK_200));
    assertThat(response.entity(), is("Hello World!"));
  }

}
