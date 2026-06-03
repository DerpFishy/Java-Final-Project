package todo.event;

import todo.task.Task;

public record TaskCreatedEvent(Task task) {
    
}