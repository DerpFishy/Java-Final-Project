package todo.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.h2.jdbcx.JdbcDataSource;

import todo.auth.AuthService;
import todo.auth.DatabaseInitializer;
import todo.auth.H2UserRepository;
import todo.auth.User;
import todo.event.EventBus;
import todo.service.TaskService;
import todo.task.FileTaskRepository;
import todo.task.TaskRepository;

/** Separate entry point for launching the Swing calendar interface. */
public final class DesktopMain {

  private DesktopMain() {
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      useSystemLookAndFeel();

      try {
        Files.createDirectories(Path.of("data"));

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:./data/users");
        DatabaseInitializer.initialize(ds);

        AuthService authService = new AuthService(new H2UserRepository(ds));
        new DesktopLoginFrame(authService, user -> openTodoApp(user)).setVisible(true);
      } catch (SQLException | IOException exception) {
        throw new IllegalStateException("Unable to initialize the desktop login flow.",
            exception);
      }
    });
  }

  private static void openTodoApp(User user) {
    EventBus eventBus = new EventBus();
    Path repositoryPath = Path.of("tasks-" + user.getUsername() + ".dat");
    TaskRepository repository = new FileTaskRepository(repositoryPath);
    TaskService taskService = new TaskService(repository, eventBus);
    new TodoFrame(taskService).setVisible(true);
  }

  private static void useSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
      // The default Swing look and feel remains usable.
    }
  }
}
