package todo.task;

public class Task {

    private final String id;
    private String title;
    private Priority priority;
    private TaskStatus status;

    public Task(String id, String title, Priority priority) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
    }

    public void complete() {
        status = TaskStatus.COMPLETED;
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

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", status, title, priority);
    }
}