package todo.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import todo.event.EventBus;
import todo.task.InMemoryTaskRepository;
import todo.task.Priority;
import todo.task.Task;
import todo.task.TaskRepository;
import todo.task.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskServiceTest {

  private TaskService service;

  @BeforeEach
  void setUp() {
    TaskRepository repo = new InMemoryTaskRepository();
    service = new TaskService(repo, new EventBus());
  }

  @Test
  void createTask_storesTaskWithCorrectFields() {
    LocalDate due = LocalDate.of(2026, 12, 31);
    Task task = service.createTask("Buy milk", Priority.LOW, due, "Errands");

    assertEquals("Buy milk", task.getTitle());
    assertEquals(Priority.LOW, task.getPriority());
    assertEquals(due, task.getDueDate());
    assertEquals("Errands", task.getProject());
    assertEquals(TaskStatus.PENDING, task.getStatus());
  }

  @Test
  void completeTask_changesStatusToCompleted() {
    Task task = service.createTask("Read book", Priority.MEDIUM, null, null);
    service.completeTask(task.getId());

    Task updated = service.getTasks().stream()
        .filter(t -> t.getId().equals(task.getId()))
        .findFirst()
        .orElseThrow();
    assertEquals(TaskStatus.COMPLETED, updated.getStatus());
  }

  @Test
  void deleteTask_removesTaskFromList() {
    Task task = service.createTask("Temp task", Priority.LOW, null, null);
    service.deleteTask(task.getId());

    assertTrue(service.getTasks().stream().noneMatch(t -> t.getId().equals(task.getId())));
  }

  @Test
  void editTask_updatesOnlyProvidedFields() {
    Task task = service.createTask("Old title", Priority.LOW, null, "ProjectA");
    service.editTask(task.getId(), "New title", null, null, null);

    Task updated = service.getTasks().stream()
        .filter(t -> t.getId().equals(task.getId()))
        .findFirst()
        .orElseThrow();
    assertEquals("New title", updated.getTitle());
    assertEquals(Priority.LOW, updated.getPriority()); // unchanged
    assertEquals("ProjectA", updated.getProject());    // unchanged
  }

  @Test
  void getTasksSortedByDate_ordersAscending() {
    service.createTask("C", Priority.LOW, LocalDate.of(2026, 6, 1), null);
    service.createTask("A", Priority.LOW, LocalDate.of(2026, 1, 1), null);
    service.createTask("B", Priority.LOW, LocalDate.of(2026, 3, 1), null);

    List<Task> sorted = service.getTasksSortedByDate();
    assertEquals("A", sorted.get(0).getTitle());
    assertEquals("B", sorted.get(1).getTitle());
    assertEquals("C", sorted.get(2).getTitle());
  }

  @Test
  void searchTasksByKeyword_matchesTitleAndProjectIgnoringCase() {
    service.createTask("Buy milk", Priority.LOW, null, "Errands");
    service.createTask("Write report", Priority.HIGH, null, "School");
    service.createTask("Clean room", Priority.MEDIUM, null, "Home");

    List<Task> titleMatches = service.searchTasksByKeyword("MILK");
    assertEquals(1, titleMatches.size());
    assertEquals("Buy milk", titleMatches.get(0).getTitle());

    List<Task> projectMatches = service.searchTasksByKeyword("school");
    assertEquals(1, projectMatches.size());
    assertEquals("Write report", projectMatches.get(0).getTitle());
  }

  @Test
  void searchTasksByKeyword_returnsEmptyListForBlankKeyword() {
    service.createTask("Buy milk", Priority.LOW, null, "Errands");

    assertTrue(service.searchTasksByKeyword("").isEmpty());
    assertTrue(service.searchTasksByKeyword("   ").isEmpty());
    assertTrue(service.searchTasksByKeyword(null).isEmpty());
  }

  @Test
  void getTasksSortedByProject_ordersAlphabetically() {
    service.createTask("t1", Priority.LOW, null, "Zebra");
    service.createTask("t2", Priority.LOW, null, "Alpha");
    service.createTask("t3", Priority.LOW, null, "Mango");

    List<Task> sorted = service.getTasksSortedByProject();
    assertEquals("Alpha", sorted.get(0).getProject());
    assertEquals("Mango", sorted.get(1).getProject());
    assertEquals("Zebra", sorted.get(2).getProject());
  }

  @Test
  void nullDueDateTasksAreLast_inDateSort() {
    service.createTask("no-date", Priority.LOW, null, null);
    service.createTask("has-date", Priority.LOW, LocalDate.of(2026, 1, 1), null);

    List<Task> sorted = service.getTasksSortedByDate();
    assertFalse(sorted.get(0).getTitle().equals("no-date"),
        "Task with null due date should be last");
  }
}
