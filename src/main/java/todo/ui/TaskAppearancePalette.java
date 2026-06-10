package todo.ui;

import java.awt.Color;
import java.time.LocalDate;

import todo.task.Task;
import todo.task.TaskStatus;

/** Centralizes status-based task colors and priority intensities for the UI. */
public final class TaskAppearancePalette {

  private static final Color COMPLETED_COLOR = new Color(57, 181, 74);
  private static final Color OVERDUE_COLOR = new Color(220, 53, 69);
  private static final Color DUE_SOON_COLOR = new Color(255, 167, 38);
  private static final Color ONGOING_COLOR = new Color(255, 215, 0);

  private TaskAppearancePalette() {
  }

  public static Color getStatusColor(Task task, TodoUiConfig config) {
    if (task == null) {
      return Color.WHITE;
    }

    Color baseColor = baseColorFor(task, config);
    return new Color(
        baseColor.getRed(),
        baseColor.getGreen(),
        baseColor.getBlue(),
        opacityFor(task));
  }

  public static Color getStatusColor(Task task, TodoUiConfig config, float ignoredIntensity) {
    return getStatusColor(task, config);
  }

  public static Color getLabelColor(Task task, boolean isSelected, TodoUiConfig config) {
    if (isSelected) {
      return Color.WHITE;
    }

    Color baseColor = baseColorFor(task, config);
    int luminance = (baseColor.getRed() * 299 + baseColor.getGreen() * 587
        + baseColor.getBlue() * 114) / 1000;
    return luminance > 180 ? new Color(32, 37, 44) : Color.WHITE;
  }

  public static boolean isOverdue(Task task) {
    if (task == null || task.getStatus() == TaskStatus.COMPLETED) {
      return false;
    }

    LocalDate dueDate = task.getDueDate();
    return dueDate != null && dueDate.isBefore(LocalDate.now());
  }

  public static float priorityIntensity(Task task) {
    if (task == null) {
      return 1.0f;
    }

    return switch (task.getPriority()) {
      case HIGH -> 1.15f;
      case MEDIUM -> 1.0f;
      case LOW -> 0.92f;
      default -> 1.0f;
    };
  }

  private static Color baseColorFor(Task task, TodoUiConfig config) {
    if (task.getStatus() == TaskStatus.COMPLETED) {
      return COMPLETED_COLOR;
    }

    LocalDate today = LocalDate.now();
    LocalDate dueDate = task.getDueDate();

    if (isOverdue(task)) {
      return OVERDUE_COLOR;
    }

    if (dueDate != null
        && !dueDate.isBefore(today)
        && dueDate.isBefore(today.plusDays(config.getWarningDaysBeforeDueDate() + 1))) {
      return DUE_SOON_COLOR;
    }

    return ONGOING_COLOR;
  }

  private static int opacityFor(Task task) {
    return switch (task.getPriority()) {
      case HIGH -> 255;
      case MEDIUM -> 220;
      case LOW -> 180;
    };
  }
}
