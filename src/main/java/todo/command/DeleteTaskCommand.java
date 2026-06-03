package todo.command;

import todo.service.TaskService;

/** Command that removes a task by id. */
public class DeleteTaskCommand implements Command {

  private final TaskService service;
  private final String taskId;

  public DeleteTaskCommand(TaskService service, String taskId) {
    this.service = service;
    this.taskId = taskId;
  }

  @Override
  public void execute() {
    service.deleteTask(taskId);
  }
}
