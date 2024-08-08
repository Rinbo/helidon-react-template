package dev.borjessons.helidon.react.template;

import java.util.List;
import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.borjessons.helidon.react.template.data.model.Role;
import dev.borjessons.helidon.react.template.data.model.User;
import dev.borjessons.helidon.react.template.data.repository.UserRepository;
import dev.borjessons.helidon.react.template.dto.RegistrationForm;
import dev.borjessons.helidon.react.template.utils.JsonUtils;
import io.helidon.common.context.Contexts;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import jakarta.json.JsonArray;

class UserServiceTest extends ServerTestBase {
  public UserServiceTest(Http1Client client) {
    super(client);
  }

  @BeforeAll
  static void beforeAll() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    userRepository.createUser(new RegistrationForm("robin.b@ex.com", "Robin"));
  }

  private static void verifyUser(User user) {
    MatcherAssert.assertThat(user.name(), Matchers.is("Robin"));
    MatcherAssert.assertThat(user.email(), Matchers.is("robin.b@ex.com"));
    MatcherAssert.assertThat(user.roles().getFirst().name(), Matchers.is("USER"));
  }

  // TODO expose in service
  @Test
  void getUserByEmail() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    Optional<User> userOption = userRepository.findByEmail("robin.b@ex.com");

    MatcherAssert.assertThat(userOption.isPresent(), Matchers.is(true));

    verifyUser(userOption.get());
  }

  // TODO expose in service
  @Test
  void getUserById() {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    Optional<User> userOption = userRepository.findById(1);

    MatcherAssert.assertThat(userOption.isPresent(), Matchers.is(true));

    verifyUser(userOption.get());
  }

  @Test
  void testGetUsers() {
    try (Http1ClientResponse response = client.get("/api/v1/users").request()) {
      MatcherAssert.assertThat(response.status(), Matchers.is(Status.OK_200));
      List<User> users = JsonUtils.fromJsonList(response.as(JsonArray.class), User.class);
      verifyUser(users.getFirst());
    }
  }

  @Test
  void updateUserRolesTest() throws InterruptedException {
    UserRepository userRepository = Contexts.globalContext().get(UserRepository.class).orElseThrow();
    User user = userRepository.findById(1).orElseThrow();
    MatcherAssert.assertThat(user.roles().size(), Matchers.is(1));
    MatcherAssert.assertThat(user.roles(), Matchers.contains(Role.USER));

    try (Http1ClientResponse response = client.put("/api/v1/users/1/roles").submit(List.of(Role.USER, Role.ADMIN))) {
      MatcherAssert.assertThat(response.status(), Matchers.is(Status.CREATED_201));

      Thread.sleep(500); // Wait for async cache to finish invalidation
      List<Role> roles = userRepository.findById(1).orElseThrow().roles();
      MatcherAssert.assertThat(roles.size(), Matchers.is(2));
      MatcherAssert.assertThat(roles, Matchers.contains(Role.USER, Role.ADMIN));
    }

    try (Http1ClientResponse response = client.put("/api/v1/users/1/roles").submit(List.of())) {
      MatcherAssert.assertThat(response.status(), Matchers.is(Status.CREATED_201));

      Thread.sleep(500);
      List<Role> roles = userRepository.findById(1).orElseThrow().roles();
      MatcherAssert.assertThat(roles.size(), Matchers.is(0));
      MatcherAssert.assertThat(roles.isEmpty(), Matchers.is(true));
    }

    try (Http1ClientResponse response = client.put("/api/v1/users/1/roles").submit(List.of(Role.USER, Role.ADMIN, Role.WEBMASTER))) {
      MatcherAssert.assertThat(response.status(), Matchers.is(Status.CREATED_201));

      Thread.sleep(500);
      List<Role> roles = userRepository.findById(1).orElseThrow().roles();
      MatcherAssert.assertThat(roles.size(), Matchers.is(3));
      MatcherAssert.assertThat(roles, Matchers.contains(Role.USER, Role.ADMIN, Role.WEBMASTER));
    }
  }
}
