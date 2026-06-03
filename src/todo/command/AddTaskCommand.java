package todo.command;

import todo.service.TaskService;
import todo.task.Priority;

public class AddTaskCommand implements Command {

    private final TaskService service;
    private final String title;
    private final Priority priority;

    public AddTaskCommand(
            TaskService service,
            String title,
            Priority priority) {

        this.service = service;
        this.title = title;
        this.priority = priority;
    }

    @Override
    public void execute() {
        service.createTask(title, priority);
    }
}