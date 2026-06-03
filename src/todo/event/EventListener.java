package todo.event;

public interface EventListener<T> {
    void onEvent(T event);
}