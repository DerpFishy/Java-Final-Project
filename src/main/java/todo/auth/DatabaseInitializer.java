package todo.auth;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/** Utility for initializing the user database schema. */
public final class DatabaseInitializer {

  private DatabaseInitializer() {}

  /** Creates the {@code users} table if it does not already exist. */
  public static void initialize(DataSource dataSource) throws SQLException {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          """
          CREATE TABLE IF NOT EXISTS users (
            id            INT AUTO_INCREMENT PRIMARY KEY,
            username      VARCHAR(255) UNIQUE NOT NULL,
            password_hash VARCHAR(255)        NOT NULL,
            email         VARCHAR(255) UNIQUE NOT NULL
          )
          """);
    }
  }
}
