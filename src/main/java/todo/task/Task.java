package todo.task;

import java.io.Serializable;
import java.time.LocalDate;

/** Represents a single to-do item. */
public class Task implements Serializable {

  private final String id;
  private String title;
  private Priority priority;
  private TaskStatus status;
  private LocalDate dueDate;
  private String project;

  public Task(String id, String title, Priority priority, LocalDate dueDate, String project) {
    this.id = id;
    this.title = title;
    this.priority = priority;
    this.dueDate = dueDate;
    this.project = project;
    this.status = TaskStatus.PENDING;
  }

  /** Marks this task as completed. */
  public void complete() {
    status = TaskStatus.COMPLETED;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Priority getPriority() {
    return priority;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public String getProject() {
    return project;
  }

  @Override
  public String toString() {
    return String.format(
        "[%s] %s | priority=%s | due=%s | project=%s",
        status, title, priority, dueDate, project);
  }
}
