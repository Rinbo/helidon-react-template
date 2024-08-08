package dev.borjessons.helidon.react.template.service.v1;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.GenericType;
import io.helidon.common.context.Contexts;
import dev.borjessons.helidon.react.template.data.model.Role;
import dev.borjessons.helidon.react.template.data.model.User;
import dev.borjessons.helidon.react.template.data.repository.UserRepository;
import dev.borjessons.helidon.react.template.dto.EditUserForm;
import dev.borjessons.helidon.react.template.dto.RegistrationForm;
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
    httpRules.get("/users", this::getPaginatedUsers);
    httpRules.get("/users/{userId}", this::getUser);
    httpRules.put("/users/{userId}", this::editUser);
    httpRules.delete("/users/{userId}", this::deleteUser);
    httpRules.post("/users", this::createUser);
    httpRules.put("/users/{userId}/roles", this::updateUserRoles);
  }

  /**
   * Create a user
   *
   * @param request  the request containing a {@link RegistrationForm}
   * @param response ok response if user was created
   */
  private void createUser(ServerRequest request, ServerResponse response) {
    userRepository.createUser(request.content().as(RegistrationForm.class));
    response.status(Status.CREATED_201).send();
  }

  private void deleteUser(ServerRequest request, ServerResponse response) {
    int userId = request.path().pathParameters().first("userId").asInt().orElseThrow();

    if (!userRepository.deleteById(userId)) {
      response.status(Status.INTERNAL_SERVER_ERROR_500).send();
      return;
    }

    response.status(Status.NO_CONTENT_204).send();
  }

  private void editUser(ServerRequest request, ServerResponse response) {
    int userId = request.path().pathParameters().first("userId").asInt().orElseThrow();
    EditUserForm editUserForm = request.content().as(EditUserForm.class);

    if (!userRepository.updateUser(userId, editUserForm)) {
      response.status(Status.INTERNAL_SERVER_ERROR_500).send();
      return;
    }

    response.status(Status.NO_CONTENT_204).send();
  }

  private void getPaginatedUsers(ServerRequest request, ServerResponse response) {
    int pageSize = request.prologue().query().first("page-size").asInt().orElse(100);
    int page = request.prologue().query().first("page").asInt().orElse(0);

    response.send(userRepository.findPaginatedUsers(pageSize, page));
  }

  private void getUser(ServerRequest request, ServerResponse response) {
    int userId = request.path().pathParameters().first("userId").asInt().orElseThrow();
    response.send(userRepository.findById(userId).orElseThrow());
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

  private void updateUserRoles(ServerRequest request, ServerResponse response) {
    int userId = request.path().pathParameters().first("userId").asInt().orElseThrow();

    List<Role> roles = request.content().as(new GenericType<>() {
    });

    userRepository.updateUserRoles(userId, roles);
    response.status(Status.CREATED_201).send();
  }
}
