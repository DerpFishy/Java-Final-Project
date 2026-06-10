package todo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

import org.h2.jdbcx.JdbcDataSource;

import todo.auth.AuthService;
import todo.auth.DatabaseInitializer;
import todo.auth.H2UserRepository;
import todo.auth.User;
import todo.event.EventBus;
import todo.event.TaskCompletedEvent;
import todo.event.TaskCreatedEvent;
import todo.service.TaskService;
import todo.task.FileTaskRepository;
import todo.task.TaskRepository;
import todo.ui.ConsoleUI;
import todo.ui.LoginUI;

/** Application entry point. Wires dependencies and starts the CLI. */
public class Main {

  public static void main(String[] args) throws Exception {
    Files.createDirectories(Path.of("data"));

    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:file:./data/users");
    try {
      DatabaseInitializer.initialize(ds);
    } catch (SQLException e) {
      System.err.println("Fatal: could not initialize database. " + e.getMessage());
      System.exit(1);
    }

    Scanner scanner = new Scanner(System.in);
    AuthService authService = new AuthService(new H2UserRepository(ds));

    Optional<User> result = new LoginUI(authService, scanner).run();
    if (result.isEmpty()) {
      System.out.println("Goodbye!");
      return;
    }
    User user = result.get();

    Path storePath = Path.of("tasks-" + user.getUsername() + ".dat");

    EventBus bus = new EventBus();
    TaskRepository repository = new FileTaskRepository(storePath);
    TaskService taskService = new TaskService(repository, bus);

    bus.subscribe(
        TaskCreatedEvent.class,
        event -> System.out.println("[event] Task created: " + event.task().getTitle()));
    bus.subscribe(
        TaskCompletedEvent.class,
        event -> System.out.println("[event] Task completed: " + event.task().getTitle()));

    new ConsoleUI(taskService, scanner).run();
  }
}
