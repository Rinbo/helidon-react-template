package io.helidon.examples.quickstart.se.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbStatementQuery;
import io.helidon.dbclient.DbTransaction;
import io.helidon.examples.quickstart.se.data.model.Role;
import io.helidon.examples.quickstart.se.data.model.User;
import io.helidon.examples.quickstart.se.dto.UserForm;
import io.helidon.examples.quickstart.se.utils.Validate;

public class UserRepository {
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

  private static User extractUser(List<DbRow> rows) {
    DbRow firstRow = rows.getFirst();
    List<Role> roles = rows.stream()
        .filter(row -> row.column("authority").asOptional().isPresent())
        .map(row -> Role.valueOf(row.column("authority").getString()))
        .toList();

    return new User(
        firstRow.column("id").getInt(),
        firstRow.column("email").getString(),
        firstRow.column("name").getString(),
        firstRow.column("created_at").get(LocalDateTime.class),
        firstRow.column("updated_at").get(LocalDateTime.class),
        roles);
  }

  private static User extractUser(Map.Entry<Integer, List<DbRow>> entry) {
    return extractUser(entry.getValue());
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
    String sql = "SELECT * FROM users JOIN authorities ON users.id = authorities.user_id ORDER BY id";
    return multiSelect(dbClient.execute().createQuery(sql));
  }

  public Optional<User> findByEmail(String email) {
    Objects.requireNonNull(email, "email cannot be null");

    List<DbRow> rows = dbClient.execute()
        .createQuery("SELECT * FROM users u LEFT JOIN authorities a ON u.id = a.user_id WHERE u.email = :email")
        .addParam("email", email)
        .execute()
        .toList();

    if (rows.isEmpty()) return Optional.empty();

    return Optional.of(extractUser(rows));
  }

  public Optional<User> findById(int userId) {
    List<DbRow> rows = dbClient.execute()
        .createQuery("SELECT * FROM users u LEFT JOIN authorities a ON u.id = a.user_id WHERE u.id = :id")
        .addParam("id", userId)
        .execute()
        .toList();

    if (rows.isEmpty()) return Optional.empty();

    return Optional.of(extractUser(rows));
  }

  public List<User> findPaginatedUsers(int pageSize, int page) {
    String sql = "SELECT * FROM (SELECT * FROM users ORDER BY id LIMIT :limit OFFSET :offset) AS l_users LEFT JOIN public.authorities a ON l_users.id = a.user_id ORDER BY id";
    return multiSelect(dbClient.execute()
        .createQuery(sql)
        .addParam("limit", pageSize)
        .addParam("offset", page * pageSize));
  }

  public void updateUserRoles(int userId, List<Role> roles) {
    DbTransaction transaction = dbClient.transaction();

    transaction.createDelete("DELETE FROM authorities WHERE user_id = :userId")
        .addParam("userId", userId)
        .execute();

    if (!roles.isEmpty()) batchUpdateRoles(userId, roles, transaction);

    transaction.commit();
  }

  private List<User> multiSelect(DbStatementQuery query) {
    return query
        .execute()
        .collect(Collectors.groupingBy(row -> row.column("id").getInt(), TreeMap::new, Collectors.toList()))
        .entrySet()
        .stream()
        .map(UserRepository::extractUser)
        .toList();
  }
}
