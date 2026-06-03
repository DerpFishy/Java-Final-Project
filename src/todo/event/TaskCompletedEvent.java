package todo.event;

import todo.task.Task;

public record TaskCompletedEvent(Task task) {
}