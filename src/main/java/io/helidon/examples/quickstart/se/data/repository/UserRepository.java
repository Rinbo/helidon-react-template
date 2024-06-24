package io.helidon.examples.quickstart.se.data.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.examples.quickstart.se.data.model.Role;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.dto.UserForm;
import io.helidon.examples.quickstart.se.utils.Validate;

public class UserRepository {
  private final Logger logger = LoggerFactory.getLogger(UserRepository.class);

  private final DbClient dbClient;

  public UserRepository() {
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
  }

  private static User extractUser(Map.Entry<Integer, List<DbRow>> entry) {
    List<DbRow> rows = entry.getValue();
    DbRow firstRow = rows.getFirst();
    List<Role> roles = rows.stream()
        .filter(row -> row.column("authority").asOptional().isPresent())
        .map(row -> Role.valueOf(row.column("authority").getString()))
        .toList();

    return new User(
        entry.getKey(),
        firstRow.column("email").getString(),
        firstRow.column("name").getString(),
        firstRow.column("created_at").get(LocalDateTime.class),
        firstRow.column("updated_at").get(LocalDateTime.class),
        roles);
  }

  public void createUser(UserForm userForm) {
    Validate.fields(userForm);

    String sql = """
        WITH new_user AS (
            INSERT INTO users (email, name)
                VALUES (:email, :name)
                RETURNING id)
        INSERT
        INTO authorities (user_id, authority)
        SELECT id, 'USER'
        FROM new_user;
        """;

    dbClient.execute()
        .createInsert(sql)
        .addParam("email", userForm.email())
        .addParam("name", userForm.name())
        .execute();
  }

  public List<User> findAll() {
    return dbClient.execute()
        .createQuery("SELECT * FROM users JOIN authorities ON users.id = authorities.user_id")
        .execute()
        .collect(Collectors.groupingBy(row -> row.column("id").getInt(), HashMap::new, Collectors.toList()))
        .entrySet()
        .stream()
        .map(UserRepository::extractUser)
        .toList();
  }
}
