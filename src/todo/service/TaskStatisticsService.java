package todo.service;

import todo.task.*;

public class TaskStatisticsService {

    private final TaskRepository repository;

    public TaskStatisticsService(
            TaskRepository repository) {

        this.repository = repository;
    }

    public long completedCount() {

        return repository.findAll()
                .stream()
                .filter(t ->
                        t.getStatus() ==
                                TaskStatus.COMPLETED)
                .count();
    }
}