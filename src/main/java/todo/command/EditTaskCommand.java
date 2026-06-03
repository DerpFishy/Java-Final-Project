package todo.command;

import java.time.LocalDate;

import todo.service.TaskService;
import todo.task.Priority;

/** Command that edits mutable fields of an existing task. Null arguments are ignored. */
public class EditTaskCommand implements Command {

  private final TaskService service;
  private final String taskId;
  private final String title;
  private final Priority priority;
  private final LocalDate dueDate;
  private final String project;

  public EditTaskCommand(
      TaskService service,
      String taskId,
      String title,
      Priority priority,
      LocalDate dueDate,
      String project) {
    this.service = service;
    this.taskId = taskId;
    this.title = title;
    this.priority = priority;
    this.dueDate = dueDate;
    this.project = project;
  }

  @Override
  public void execute() {
    service.editTask(taskId, title, priority, dueDate, project);
  }
}
