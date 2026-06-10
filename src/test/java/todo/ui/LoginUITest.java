package todo.ui;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import todo.auth.AuthService;
import todo.auth.DatabaseInitializer;
import todo.auth.H2UserRepository;
import todo.auth.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUITest {

  private AuthService authService;

  @BeforeEach
  void setUp() throws SQLException {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:login_ui_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
    DatabaseInitializer.initialize(ds);
    authService = new AuthService(new H2UserRepository(ds));
  }

  @Test
  void run_registersAndReturnsUser() {
    String input = String.join("\n",
        "2",
        "uiuser",
        "uiuser@example.com",
        "password123",
        "3") + "\n";

    LoginUI loginUI = new LoginUI(authService, new java.util.Scanner(
        new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));

    Optional<User> result = loginUI.run();

    assertTrue(result.isPresent());
    assertEquals("uiuser", result.get().getUsername());
  }

  @Test
  void run_logsInExistingUser() {
    authService.register("loginuser", "login@example.com", "password123");

    String input = String.join("\n",
        "1",
        "loginuser",
        "password123",
        "3") + "\n";

    LoginUI loginUI = new LoginUI(authService, new java.util.Scanner(
        new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));

    Optional<User> result = loginUI.run();

    assertTrue(result.isPresent());
    assertEquals("loginuser", result.get().getUsername());
  }
}
