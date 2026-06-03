package todo.command;

import java.time.LocalDate;

import todo.service.TaskService;
import todo.task.Priority;

/** Command that creates a new task. */
public class AddTaskCommand implements Command {

  private final TaskService service;
  private final String title;
  private final Priority priority;
  private final LocalDate dueDate;
  private final String project;

  public AddTaskCommand(
      TaskService service,
      String title,
      Priority priority,
      LocalDate dueDate,
      String project) {
    this.service = service;
    this.title = title;
    this.priority = priority;
    this.dueDate = dueDate;
    this.project = project;
  }

  @Override
  public void execute() {
    service.createTask(title, priority, dueDate, project);
  }
}
