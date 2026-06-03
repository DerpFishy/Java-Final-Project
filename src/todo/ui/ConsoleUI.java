package todo.ui;

import todo.service.TaskService;

public class ConsoleUI {

    private final TaskService service;

    public ConsoleUI(TaskService service) {
        this.service = service;
    }

    public void showTasks() {

        System.out.println("\nTasks:");

        service.getTasks()
                .forEach(System.out::println);
    }
}