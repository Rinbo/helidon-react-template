package io.helidon.examples.quickstart.se.data.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;
import io.helidon.examples.quickstart.se.dto.UserForm;
import io.helidon.examples.quickstart.se.utils.Validate;
import jakarta.json.JsonObject;

public class UserRepository {
  private final Logger logger = LoggerFactory.getLogger(UserRepository.class);

  private final DbClient dbClient;

  public UserRepository() {
    dbClient = Contexts.globalContext().get(DbClient.class).orElseThrow();
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

  public List<JsonObject> findAll() {
    return dbClient.execute()
        .createQuery("SELECT * FROM users")
        .execute()
        .map(dbRow -> dbRow.as(JsonObject.class))
        .toList();
  }
}
