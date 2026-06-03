package todo.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persists tasks to a file via Java serialization.
 * Delegates in-memory lookups to a {@link LinkedHashMap} so insertion order is preserved.
 */
public class FileTaskRepository implements TaskRepository {

  private final Path storePath;
  private final Map<String, Task> tasks;

  public FileTaskRepository(Path storePath) {
    this.storePath = storePath;
    this.tasks = loadFromDisk();
  }

  @Override
  public void save(Task task) {
    tasks.put(task.getId(), task);
    flush();
  }

  @Override
  public void delete(String id) {
    tasks.remove(id);
    flush();
  }

  @Override
  public Optional<Task> findById(String id) {
    return Optional.ofNullable(tasks.get(id));
  }

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks.values());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Task> loadFromDisk() {
    if (!Files.exists(storePath)) {
      return new LinkedHashMap<>();
    }
    try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(storePath))) {
      return (Map<String, Task>) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Warning: could not load saved tasks — starting fresh. " + e.getMessage());
      return new LinkedHashMap<>();
    }
  }

  private void flush() {
    try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(storePath))) {
      out.writeObject(tasks);
    } catch (IOException e) {
      System.err.println("Warning: failed to save tasks. " + e.getMessage());
    }
  }
}
