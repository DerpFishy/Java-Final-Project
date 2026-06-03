package todo.auth;

import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

  private AuthService service;

  @BeforeEach
  void setUp() throws SQLException {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:auth_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
    DatabaseInitializer.initialize(ds);
    service = new AuthService(new H2UserRepository(ds));
  }

  @Test
  void register_storesUserWithHashedPassword() {
    User user = service.register("alice", "alice@example.com", "password1");

    assertNotNull(user);
    assertEquals("alice", user.getUsername());
    assertTrue(BCrypt.checkpw("password1", user.getPasswordHash()));
  }

  @Test
  void register_throwsAuthException_whenUsernameAlreadyTaken() {
    service.register("bob", "bob@example.com", "password1");

    assertThrows(AuthException.class,
        () -> service.register("bob", "bob2@example.com", "password1"));
  }

  @Test
  void register_throwsAuthException_whenEmailAlreadyRegistered() {
    service.register("carol", "carol@example.com", "password1");

    assertThrows(AuthException.class,
        () -> service.register("carol2", "carol@example.com", "password1"));
  }

  @Test
  void register_throwsAuthException_whenUsernameBlank() {
    assertThrows(AuthException.class,
        () -> service.register("  ", "x@x.com", "password1"));
  }

  @Test
  void register_throwsAuthException_whenPasswordTooShort() {
    assertThrows(AuthException.class,
        () -> service.register("dave", "dave@example.com", "short"));
  }

  @Test
  void login_returnsUser_whenCredentialsMatch() {
    service.register("eve", "eve@example.com", "mypassword");
    User user = service.login("eve", "mypassword");

    assertEquals("eve", user.getUsername());
  }

  @Test
  void login_throwsAuthException_whenUsernameNotFound() {
    AuthException ex = assertThrows(AuthException.class,
        () -> service.login("nobody", "password1"));
    assertEquals("Invalid username or password.", ex.getMessage());
  }

  @Test
  void login_throwsAuthException_whenPasswordWrong() {
    service.register("frank", "frank@example.com", "correctpass");

    AuthException ex = assertThrows(AuthException.class,
        () -> service.login("frank", "wrongpass"));
    assertEquals("Invalid username or password.", ex.getMessage());
  }
}
