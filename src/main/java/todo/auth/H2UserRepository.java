package todo.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

/** H2-backed implementation of {@link UserRepository} using JDBC. */
public class H2UserRepository implements UserRepository {

  private final DataSource dataSource;

  public H2UserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void save(User user) {
    String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, user.getUsername());
      ps.setString(2, user.getPasswordHash());
      ps.setString(3, user.getEmail());
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new AuthException("Failed to save user: " + e.getMessage());
    }
  }

  @Override
  public void update(User user) {
    String sql = "UPDATE users SET password_hash = ?, email = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, user.getPasswordHash());
      ps.setString(2, user.getEmail());
      ps.setInt(3, user.getId());
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new AuthException("Failed to update user: " + e.getMessage());
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    String sql = "SELECT id, username, password_hash, email FROM users WHERE username = ?";
    return queryOne(sql, username);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    String sql = "SELECT id, username, password_hash, email FROM users WHERE email = ?";
    return queryOne(sql, email);
  }

  private Optional<User> queryOne(String sql, String param) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, param);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRow(rs));
        }
      }
      return Optional.empty();
    } catch (SQLException e) {
      throw new AuthException("Database query failed: " + e.getMessage());
    }
  }

  private User mapRow(ResultSet rs) throws SQLException {
    return new User(
        rs.getInt("id"),
        rs.getString("username"),
        rs.getString("password_hash"),
        rs.getString("email"));
  }
}
