package todo.service;

import todo.event.*;
import todo.task.*;
import todo.util.IdGenerator;

import java.util.List;

public class TaskService {

    private final TaskRepository repository;
    private final EventBus eventBus;

    public TaskService(
            TaskRepository repository,
            EventBus eventBus) {

        this.repository = repository;
        this.eventBus = eventBus;
    }

    public Task createTask(
            String title,
            Priority priority) {

        Task task = new Task(
                IdGenerator.generate(),
                title,
                priority);

        repository.save(task);

        eventBus.publish(
                new TaskCreatedEvent(task));

        return task;
    }

    public void completeTask(String id) {

        repository.findById(id)
                .ifPresent(task -> {

                    task.complete();

                    eventBus.publish(
                            new TaskCompletedEvent(task));
                });
    }

    public void deleteTask(String id) {
        repository.delete(id);
    }

    public List<Task> getTasks() {
        return repository.findAll();
    }
}