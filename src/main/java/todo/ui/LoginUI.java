package todo.ui;

import java.io.Console;
import java.util.Optional;
import java.util.Scanner;

import todo.auth.AuthException;
import todo.auth.AuthService;
import todo.auth.User;

/**
 * Text-based login and registration screen.
 * Runs before {@link ConsoleUI} and returns the authenticated {@link User}.
 */
public class LoginUI {

  private static final String DIVIDER = "─".repeat(60);

  private final AuthService authService;
  private final Scanner scanner;

  public LoginUI(AuthService authService, Scanner scanner) {
    this.authService = authService;
    this.scanner = scanner;
  }

  /**
   * Displays the login/register menu and blocks until the user authenticates or quits.
   *
   * @return the authenticated user, or {@link Optional#empty()} if the user chose to quit
   */
  public Optional<User> run() {
    System.out.println("=== Todo List — Login ===");
    while (true) {
      printMenu();
      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1" -> {
          Optional<User> user = handleLogin();
          if (user.isPresent()) {
            return user;
          }
        }
        case "2" -> {
          Optional<User> user = handleRegister();
          if (user.isPresent()) {
            return user;
          }
        }
        case "3" -> {
          return Optional.empty();
        }
        default -> System.out.println("Unknown option. Try again.");
      }
    }
  }

  private void printMenu() {
    System.out.println("\n" + DIVIDER);
    System.out.println(" 1) Login");
    System.out.println(" 2) Register");
    System.out.println(" 3) Quit");
    System.out.print("Choose: ");
  }

  private Optional<User> handleLogin() {
    String username = prompt("Username");
    String password = readPassword("Password");
    try {
      User user = authService.login(username, password);
      System.out.println("Welcome back, " + user.getUsername() + "!");
      return Optional.of(user);
    } catch (AuthException e) {
      System.out.println("Login failed: " + e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<User> handleRegister() {
    String username = prompt("Username");
    String email = prompt("Email");
    String password = readPassword("Password (min 8 chars)");
    try {
      User user = authService.register(username, email, password);
      System.out.println("Account created. Welcome, " + user.getUsername() + "!");
      return Optional.of(user);
    } catch (AuthException e) {
      System.out.println("Registration failed: " + e.getMessage());
      return Optional.empty();
    }
  }

  private String prompt(String label) {
    System.out.print(label + ": ");
    return scanner.nextLine().trim();
  }

  private String readPassword(String label) {
    System.out.print(label + ": ");
    Console console = System.console();
    if (console != null) {
      return new String(console.readPassword());
    }
    return scanner.nextLine().trim();
  }
}
