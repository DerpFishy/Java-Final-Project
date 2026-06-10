package todo.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import todo.command.AddTaskCommand;
import todo.command.CompleteTaskCommand;
import todo.command.DeleteTaskCommand;
import todo.command.EditTaskCommand;
import todo.service.TaskService;
import todo.task.Priority;
import todo.task.Task;

/**
 * Text-based command-line interface.
 * Reads user input and dispatches to {@link TaskService} via command objects.
 */
public class ConsoleUI {

  private static final String DIVIDER = "─".repeat(60);

  private final TaskService service;
  private final Scanner scanner;

  public ConsoleUI(TaskService service, Scanner scanner) {
    this.service = service;
    this.scanner = scanner;
  }

  /** Starts the interactive menu loop. Runs until the user chooses to quit. */
  public void run() {
    System.out.println("=== Todo List ===");
    boolean running = true;
    while (running) {
      printMenu();
      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1" -> handleAdd();
        case "2" -> handleEdit();
        case "3" -> handleComplete();
        case "4" -> handleDelete();
        case "5" -> showTasks(service.getTasks(), "All Tasks");
        case "6" -> showTasks(service.getTasksSortedByDate(), "Tasks sorted by due date");
        case "7" -> showTasks(service.getTasksSortedByProject(), "Tasks sorted by project");
        case "8" -> handleSearch();
        case "9" -> handleFilterByPriority();
        case "0" -> running = false;
        default -> System.out.println("Unknown option. Try again.");
      }
    }
    System.out.println("Goodbye!");
  }

  private void printMenu() {
    System.out.println("\n" + DIVIDER);
    System.out.println(" 1) Add task");
    System.out.println(" 2) Edit task");
    System.out.println(" 3) Mark task complete");
    System.out.println(" 4) Delete task");
    System.out.println(" 5) List all tasks");
    System.out.println(" 6) List sorted by due date");
    System.out.println(" 7) List sorted by project");
    System.out.println(" 8) Search tasks by keyword");
    System.out.println(" 9) Filter by priority");
    System.out.println(" 0) Quit");
    System.out.print("Choose: ");
  }

  private void handleAdd() {
    String title = prompt("Title");
    Priority priority = promptPriority();
    LocalDate dueDate = promptDate("Due date (yyyy-mm-dd)");
    String project = prompt("Project (or blank to skip)");
    new AddTaskCommand(service, title, priority, dueDate, project.isBlank() ? null : project)
        .execute();
    System.out.println("Task added.");
  }

  private void handleEdit() {
    String id = prompt("Task ID to edit");
    System.out.println("Leave blank to keep current value.");
    String title = prompt("New title");
    Priority priority = promptPriorityOptional();
    LocalDate dueDate = promptDate("New due date (yyyy-mm-dd)");
    String project = prompt("New project");
    new EditTaskCommand(
            service,
            id,
            title.isBlank() ? null : title,
            priority,
            dueDate,
            project.isBlank() ? null : project)
        .execute();
    System.out.println("Task updated.");
  }

  private void handleComplete() {
    String id = prompt("Task ID to mark complete");
    new CompleteTaskCommand(service, id).execute();
    System.out.println("Task marked complete.");
  }

  private void handleDelete() {
    String id = prompt("Task ID to delete");
    new DeleteTaskCommand(service, id).execute();
    System.out.println("Task deleted.");
  }

  private void handleSearch() {
    String keyword = prompt("Keyword");
    if (keyword.isBlank()) {
      System.out.println("Keyword cannot be empty.");
      return;
    }
    showTasks(service.searchTasksByKeyword(keyword), "Search results for \"" + keyword + "\"");
  }

  private void handleFilterByPriority() {
    Priority priority = promptPriority();
    showTasks(service.getTasksByPriority(priority), "Tasks with priority " + priority);
  }

  private void showTasks(List<Task> tasks, String header) {
    System.out.println("\n" + header + ":");
    if (tasks.isEmpty()) {
      System.out.println("  (none)");
      return;
    }
    tasks.forEach(t -> System.out.println("  " + t.getId() + " | " + t));
  }

  private String prompt(String label) {
    System.out.print(label + ": ");
    return scanner.nextLine().trim();
  }

  private Priority promptPriority() {
    while (true) {
      System.out.print("Priority (LOW / MEDIUM / HIGH): ");
      String input = scanner.nextLine().trim().toUpperCase();
      try {
        return Priority.valueOf(input);
      } catch (IllegalArgumentException e) {
        System.out.println("Invalid priority. Enter LOW, MEDIUM, or HIGH.");
      }
    }
  }

  private Priority promptPriorityOptional() {
    System.out.print("Priority (LOW / MEDIUM / HIGH, or blank to keep): ");
    String input = scanner.nextLine().trim().toUpperCase();
    if (input.isBlank()) {
      return null;
    }
    try {
      return Priority.valueOf(input);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid priority — keeping current.");
      return null;
    }
  }

  private LocalDate promptDate(String label) {
    System.out.print(label + ": ");
    String input = scanner.nextLine().trim();
    if (input.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(input);
    } catch (DateTimeParseException e) {
      System.out.println("Invalid date format — skipping.");
      return null;
    }
  }
}
