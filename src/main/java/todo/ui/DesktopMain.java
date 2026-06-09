package todo.ui;

import java.nio.file.Path;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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

      EventBus eventBus = new EventBus();
      TaskRepository repository = new FileTaskRepository(Path.of("tasks.dat"));
      TaskService taskService = new TaskService(repository, eventBus);
      new TodoFrame(taskService).setVisible(true);
    });
  }

  private static void useSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
      // The default Swing look and feel remains usable.
    }
  }
}
