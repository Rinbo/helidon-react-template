package dev.borjessons.helidon.react.template.data.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.borjessons.helidon.react.template.data.cache.UserCache;
import dev.borjessons.helidon.react.template.data.model.Role;
import dev.borjessons.helidon.react.template.data.model.User;
import dev.borjessons.helidon.react.template.dto.EditUserForm;
import dev.borjessons.helidon.react.template.dto.RegistrationForm;
import dev.borjessons.helidon.react.template.notify.CacheInvalidatorNotifier;
import dev.borjessons.helidon.react.template.utils.Validate;
import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.DbStatementQuery;
import io.helidon.dbclient.DbTransaction;

public class UserRepository {
  private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

  private final DbClient dbClient;
  private final UserCache userCache;
  private final CacheInvalidatorNotifier cacheInvalidatorNotifier;

  public UserRepository() {
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
    userCache = Contexts.globalContext().get(UserCache.class).orElseThrow();
    cacheInvalidatorNotifier = Contexts.globalContext().get(CacheInvalidatorNotifier.class).orElseThrow();
  }

  // TODO there is some helidon DbMapper I think that can be used here.
  private static User extractUser(List<DbRow> rows) {
    DbRow firstRow = rows.getFirst();

    List<Role> roles = rows.stream()
        .filter(row -> row.column("authority").as(Optional::ofNullable).get().isPresent())
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

  public void createUser(RegistrationForm registrationForm) {
    Validate.fields(registrationForm);

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
        .addParam("email", registrationForm.email().trim().toLowerCase())
        .addParam("name", registrationForm.name().trim())
        .execute();
  }

  public boolean deleteById(int userId) {
    logger.debug("Deleting user with id: {}", userId);
    long delete = dbClient.execute().delete("DELETE FROM users WHERE id = ?", userId);
    cacheInvalidatorNotifier.invalidateUser(userId);
    return delete > 0;
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

    User user = extractUser(rows);
    userCache.put(user);
    return Optional.of(user);
  }

  public Optional<User> findById(int userId) {
    User cachedUser = userCache.get(userId);

    if (cachedUser != null) {
      logger.debug("found user in cache: {}", cachedUser);
      return Optional.of(cachedUser);
    }

    logger.debug("no user in cache. Looking up in db: {}", userId);

    List<DbRow> rows = dbClient.execute()
        .createQuery("SELECT * FROM users u LEFT JOIN authorities a ON u.id = a.user_id WHERE u.id = :id")
        .addParam("id", userId)
        .execute()
        .toList();

    User user = extractUser(rows);
    userCache.put(user);
    return Optional.of(user);
  }

  public List<User> findPaginatedUsers(int pageSize, int page) {
    String sql = "SELECT * FROM (SELECT * FROM users ORDER BY id LIMIT :limit OFFSET :offset) AS l_users LEFT JOIN public.authorities a ON l_users.id = a.user_id ORDER BY id";
    return multiSelect(dbClient.execute()
        .createQuery(sql)
        .addParam("limit", pageSize)
        .addParam("offset", page * pageSize));
  }

  public boolean updateUser(int userId, EditUserForm editUserForm) {
    DbTransaction transaction = dbClient.transaction();
    long updateCount;

    try {
      updateCount = transaction.createUpdate("UPDATE users SET name = :name, updated_at = :updatedAt WHERE id = :userId")
          .addParam("name", editUserForm.name().trim())
          .addParam("updatedAt", new Date(Instant.now().toEpochMilli()))
          .addParam("userId", userId)
          .execute();

      updateUserRoles(transaction, userId, editUserForm.roles());
      transaction.commit();
    } catch (RuntimeException e) {
      logger.warn("failed to update user with id {}", userId, e);
      transaction.rollback();
      updateCount = 0;
    }

    return updateCount > 0;
  }

  public void updateUserRoles(int userId, List<Role> roles) {
    DbTransaction transaction = dbClient.transaction();
    updateUserRoles(transaction, userId, roles);
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

  private void updateUserRoles(DbTransaction transaction, int userId, List<Role> roles) {
    try (Connection connection = transaction.unwrap(Connection.class)) {
      try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM authorities WHERE user_id = ?")) {
        deleteStatement.setInt(1, userId);
        deleteStatement.executeUpdate();
      }

      try (PreparedStatement batchStatement = connection.prepareStatement("INSERT INTO authorities (user_id, authority) VALUES (?, ?)")) {
        for (Role role : roles) {
          batchStatement.setInt(1, userId);
          batchStatement.setString(2, role.name());
          batchStatement.addBatch();
        }

        batchStatement.executeBatch();
        cacheInvalidatorNotifier.invalidateUser(userId);
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Role update failed", e);
    }
  }
}
