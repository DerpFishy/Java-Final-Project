package todo.task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    void save(Task task);

    void delete(String id);

    Optional<Task> findById(String id);

    List<Task> findAll();
}