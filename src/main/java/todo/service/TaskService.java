package todo.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import todo.event.EventBus;
import todo.event.TaskCompletedEvent;
import todo.event.TaskCreatedEvent;
import todo.task.Priority;
import todo.task.Task;
import todo.task.TaskRepository;
import todo.util.IdGenerator;

/** Core business logic for managing tasks. */
public class TaskService {

  private final TaskRepository repository;
  private final EventBus eventBus;

  public TaskService(TaskRepository repository, EventBus eventBus) {
    this.repository = repository;
    this.eventBus = eventBus;
  }

  /** Creates and persists a new task, then publishes a {@link TaskCreatedEvent}. */
  public Task createTask(
      String title, Priority priority, LocalDate dueDate, String project) {
    Task task = new Task(IdGenerator.generate(), title, priority, dueDate, project);
    repository.save(task);
    eventBus.publish(new TaskCreatedEvent(task));
    return task;
  }

  /**
   * Edits mutable fields of an existing task. Only non-null arguments are applied.
   *
   * @return the updated task, or empty if not found
   */
  public Optional<Task> editTask(
      String id, String title, Priority priority, LocalDate dueDate, String project) {
    Optional<Task> found = repository.findById(id);
    found.ifPresent(task -> {
      if (title != null) {
        task.setTitle(title);
      }
      if (priority != null) {
        task.setPriority(priority);
      }
      if (dueDate != null) {
        task.setDueDate(dueDate);
      }
      if (project != null) {
        task.setProject(project);
      }
      repository.save(task);
    });
    return found;
  }

  /** Marks a task as completed and publishes a {@link TaskCompletedEvent}. */
  public void completeTask(String id) {
    repository.findById(id).ifPresent(task -> {
      task.complete();
      eventBus.publish(new TaskCompletedEvent(task));
    });
  }

  /** Removes a task by id. */
  public void deleteTask(String id) {
    repository.delete(id);
  }

  /** Returns all tasks in insertion order. */
  public List<Task> getTasks() {
    return repository.findAll();
  }

  /** Returns tasks whose title or project contains the keyword, ignoring case. */
  public List<Task> searchTasksByKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return List.of();
    }

    String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
    return repository.findAll().stream()
        .filter(task -> containsKeyword(task.getTitle(), normalizedKeyword)
            || containsKeyword(task.getProject(), normalizedKeyword))
        .toList();
  }

  /** Returns tasks filtered by the given priority. */
  public List<Task> getTasksByPriority(Priority priority) {
    return repository.findAll().stream()
        .filter(task -> task.getPriority() == priority)
        .toList();
  }

  /** Returns all tasks sorted by due date ascending (null dates last). */
  public List<Task> getTasksSortedByDate() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(
            Task::getDueDate,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  /** Returns all tasks sorted alphabetically by project name (null project last). */
  public List<Task> getTasksSortedByProject() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(
            Task::getProject,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  private boolean containsKeyword(String value, String normalizedKeyword) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
  }
}
