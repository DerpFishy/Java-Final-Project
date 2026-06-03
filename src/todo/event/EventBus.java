package todo.event;

import java.util.*;

public class EventBus {

    private final Map<Class<?>, List<EventListener<?>>> listeners =
            new HashMap<>();

    public <T> void subscribe(
            Class<T> eventType,
            EventListener<T> listener) {

        listeners.computeIfAbsent(
                eventType,
                k -> new ArrayList<>())
                .add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {

        var eventListeners =
                listeners.getOrDefault(
                        event.getClass(),
                        List.of());

        for (var listener : eventListeners) {
            ((EventListener<T>) listener)
                    .onEvent(event);
        }
    }
}