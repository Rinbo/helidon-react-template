package io.helidon.examples.quickstart.se.service.v1;

import java.util.Collections;

import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

public class UserService implements HttpService {
  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

  @Override
  public void routing(HttpRules httpRules) {
    httpRules.get("/users", this::getUsers);
  }

  /**
   * Get all users.
   *
   * @param request  the server request
   * @param response a list of users
   */
  private void getUsers(ServerRequest request, ServerResponse response) {
    JsonObject returnObject = JSON.createObjectBuilder()
        .add("users", "Robin, Albin, Sixten och Maria")
        .build();
    response.send(returnObject);
  }
}
