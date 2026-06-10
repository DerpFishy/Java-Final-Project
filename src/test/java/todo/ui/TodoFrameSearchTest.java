package todo.ui;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import todo.task.Priority;
import todo.task.Task;

class TodoFrameSearchTest {

  @Test
  void filterVisibleTasks_matchesKeywordAndDateFilter() {
    Task report = new Task("1", "Write report", Priority.HIGH,
        LocalDate.of(2026, 6, 10), "School");
    Task milk = new Task("2", "Buy milk", Priority.LOW,
        LocalDate.of(2026, 6, 11), "Errands");

    List<Task> filtered = TodoFrame.filterVisibleTasks(
        List.of(report, milk),
        "report",
        LocalDate.of(2026, 6, 10),
        false);

    assertEquals(1, filtered.size());
    assertEquals("Write report", filtered.get(0).getTitle());
  }

  @Test
  void filterVisibleTasks_filtersByPriority() {
    Task report = new Task("1", "Write report", Priority.HIGH,
        LocalDate.of(2026, 6, 10), "School");
    Task milk = new Task("2", "Buy milk", Priority.LOW,
        LocalDate.of(2026, 6, 10), "Errands");

    List<Task> filtered = TodoFrame.filterVisibleTasks(
        List.of(report, milk),
        "",
        LocalDate.of(2026, 6, 10),
        true,
        Priority.HIGH);

    assertEquals(1, filtered.size());
    assertEquals(Priority.HIGH, filtered.get(0).getPriority());
  }

  @Test
  void filterVisibleTasks_returnsAllTasksWhenSearchIsBlank() {
    Task report = new Task("1", "Write report", Priority.HIGH,
        LocalDate.of(2026, 6, 10), "School");
    Task milk = new Task("2", "Buy milk", Priority.LOW,
        LocalDate.of(2026, 6, 11), "Errands");

    List<Task> filtered = TodoFrame.filterVisibleTasks(
        List.of(report, milk),
        "   ",
        LocalDate.of(2026, 6, 10),
        false,
        null);

    assertEquals(1, filtered.size());
    assertTrue(filtered.stream().anyMatch(task -> task.getId().equals("1")));
  }
}
