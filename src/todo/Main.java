package todo;

import todo.event.*;
import todo.service.*;
import todo.task.*;
import todo.ui.ConsoleUI;

public class Main {

    public static void main(String[] args) {

        EventBus bus = new EventBus();

        TaskRepository repository =
                new InMemoryTaskRepository();

        TaskService taskService =
                new TaskService(repository, bus);

        ConsoleUI ui =
                new ConsoleUI(taskService);

        bus.subscribe(
                TaskCreatedEvent.class,
                event -> System.out.println(
                        "Task Created: "
                                + event.task().getTitle()));

        bus.subscribe(
                TaskCompletedEvent.class,
                event -> System.out.println(
                        "Task Completed: "
                                + event.task().getTitle()));

        var task =
                taskService.createTask(
                        "Finish Java Project",
                        Priority.HIGH);

        taskService.createTask(
                "Study Design Patterns",
                Priority.MEDIUM);

        taskService.completeTask(
                task.getId());

        ui.showTasks();
    }
}