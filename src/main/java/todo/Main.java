package todo;

import java.nio.file.Path;
import java.util.Scanner;

import todo.event.EventBus;
import todo.event.TaskCompletedEvent;
import todo.event.TaskCreatedEvent;
import todo.service.TaskService;
import todo.task.FileTaskRepository;
import todo.task.TaskRepository;
import todo.ui.ConsoleUI;

/** Application entry point. Wires dependencies and starts the CLI. */
public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    Path storePath = Path.of("tasks.dat");

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
