package todo.command;

import todo.service.TaskService;

public class CompleteTaskCommand
        implements Command {

    private final TaskService service;
    private final String taskId;

    public CompleteTaskCommand(
            TaskService service,
            String taskId) {

        this.service = service;
        this.taskId = taskId;
    }

    @Override
    public void execute() {
        service.completeTask(taskId);
    }
}