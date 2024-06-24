package io.helidon.examples.quickstart.se.service.v1;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.data.repository.UserRepository;
import io.helidon.examples.quickstart.se.dto.UserForm;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;

public class UserService implements HttpService {
  private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  public UserService() {
    userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
  }

  @Override
  public void routing(HttpRules httpRules) {
    httpRules.get("/users", this::getUsers);
    httpRules.post("/users", this::createUser);
  }

  /**
   * Create a user
   *
   * @param request  the request containing a {@link UserForm}
   * @param response ok response if user was created
   */
  private void createUser(ServerRequest request, ServerResponse response) {
    userRepository.createUser(request.content().as(UserForm.class));
    response.status(Status.CREATED_201).send();
  }

  /**
   * Get all users.
   *
   * @param request  the server request
   * @param response a list of users
   */
  private void getUsers(ServerRequest request, ServerResponse response) {
    List<User> users = userRepository.findAll();
    response.send(users);
  }
}
