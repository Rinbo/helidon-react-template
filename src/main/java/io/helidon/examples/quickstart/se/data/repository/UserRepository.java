package io.helidon.examples.quickstart.se.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import io.helidon.dbclient.DbTransaction;
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

  private static void batchUpdateRoles(int userId, List<Role> roles, DbTransaction transaction) {
    try (Connection connection = transaction.unwrap(Connection.class)) {
      PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO authorities (user_id, authority) VALUES (?, ?)");

      for (Role role : roles) {
        preparedStatement.setInt(1, userId);
        preparedStatement.setString(2, role.name());
        preparedStatement.addBatch();
      }

      preparedStatement.executeBatch();

    } catch (SQLException e) {
      throw new IllegalStateException("Role update failed", e);
    }
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

  public void updateUserRoles(int userId, List<Role> roles) {
    DbTransaction transaction = dbClient.transaction();

    transaction.createDelete("DELETE FROM authorities WHERE user_id = :userId")
        .addParam("userId", userId)
        .execute();

    if (!roles.isEmpty()) batchUpdateRoles(userId, roles, transaction);

    /*
    transaction.createInsert("INSERT INTO authorities (user_id, authority) VALUES (?, ?)")
          .params(roles.stream()
              .map(role -> new Object[] {userId, role.name()})
              .toList())
          .execute();

     */

    transaction.commit();
  }
}
