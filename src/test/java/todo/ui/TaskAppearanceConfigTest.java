package todo.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import todo.task.Priority;
import todo.task.Task;

class TaskAppearanceConfigTest {

  @Test
  void defaultsShouldBeSensible() {
    TodoUiConfig config = TodoUiConfig.loadFromDefaults();

    assertEquals(3, config.getWarningDaysBeforeDueDate());
  }

  @Test
  void warningDaysRoundTripThroughConfig() {
    TodoUiConfig config = new TodoUiConfig(5);

    assertEquals(5, config.getWarningDaysBeforeDueDate());
  }

  @Test
  void defaultsShouldUseEnglishLanguage() {
    TodoUiConfig config = TodoUiConfig.loadFromDefaults();

    assertEquals("en", config.getLanguageCode());
  }

  @Test
  void defaultsFollowSystemLocaleWhenSupported() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("zh-TW"));

      TodoUiConfig config = TodoUiConfig.loadFromDefaults();

      assertEquals("zh_TW", config.getLanguageCode());
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  void defaultsFallBackToEnglishWhenUnsupported() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("fr-FR"));

      TodoUiConfig config = TodoUiConfig.loadFromDefaults();

      assertEquals("en", config.getLanguageCode());
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  void languagePreferenceRoundTripThroughConfig() {
    TodoUiConfig config = new TodoUiConfig(5, "zh_TW");

    assertEquals("zh_TW", config.getLanguageCode());
  }

  @Test
  void completedTasksUseGreenAndPriorityChangesOpacity() {
    Task highPriority = new Task("1", "Ship release", Priority.HIGH, LocalDate.now().plusDays(1), "Ops");
    Task lowPriority = new Task("2", "Ship release", Priority.LOW, LocalDate.now().plusDays(1), "Ops");
    highPriority.complete();
    lowPriority.complete();

    Color highColor = TaskAppearancePalette.getStatusColor(highPriority, new TodoUiConfig(3));
    Color lowColor = TaskAppearancePalette.getStatusColor(lowPriority, new TodoUiConfig(3));

    assertEquals(new Color(57, 181, 74, 255), highColor);
    assertEquals(new Color(57, 181, 74, 180), lowColor);
    assertTrue(highColor.getAlpha() > lowColor.getAlpha());
  }

  @Test
  void overdueTasksUseRedAndDueSoonTasksUseOrange() {
    Task overdue = new Task("2", "Fix bug", Priority.MEDIUM, LocalDate.now().minusDays(1), "Dev");
    Task dueSoon = new Task("3", "Review PR", Priority.LOW, LocalDate.now().plusDays(2), "Dev");

    Color overdueColor = TaskAppearancePalette.getStatusColor(overdue, new TodoUiConfig(3));
    Color dueSoonColor = TaskAppearancePalette.getStatusColor(dueSoon, new TodoUiConfig(3));

    assertEquals(220, overdueColor.getRed());
    assertEquals(53, overdueColor.getGreen());
    assertEquals(69, overdueColor.getBlue());
    assertEquals(255, dueSoonColor.getRed());
    assertEquals(167, dueSoonColor.getGreen());
    assertEquals(38, dueSoonColor.getBlue());
  }

  @Test
  void calendarShowsOngoingOverdueAndCompletedTaskSummary() {
    CalendarPanel panel = new CalendarPanel(date -> {
    });
    Task ongoing = new Task("1", "Draft plan", Priority.MEDIUM, LocalDate.now(), "Work");
    Task overdue = new Task("2", "Fix bug", Priority.HIGH, LocalDate.now().minusDays(1), "Work");
    Task completed = new Task("3", "Ship release", Priority.HIGH, LocalDate.now().plusDays(1), "Work");
    completed.complete();

    panel.setTasks(List.of(ongoing, overdue, completed));

    JLabel ongoingLabel = findLabel(panel, "Ongoing tasks");
    JLabel overdueLabel = findLabel(panel, "Overdue tasks");
    JLabel completedLabel = findLabel(panel, "Completed tasks");

    assertTrue(ongoingLabel.getText().contains("1"));
    assertTrue(overdueLabel.getText().contains("1"));
    assertTrue(completedLabel.getText().contains("1"));
  }

  @Test
  void calendarDayButtonsUseLargerReadabilityFont() {
    CalendarPanel panel = new CalendarPanel(date -> {
    });

    JButton dayButton = findButton(panel);

    assertTrue(dayButton.getFont().getSize() >= 13);
  }

  private JLabel findLabel(Component component, String text) {
    if (component instanceof JLabel label && label.getText() != null
        && label.getText().contains(text)) {
      return label;
    }

    if (component instanceof JPanel panel) {
      for (Component child : panel.getComponents()) {
        JLabel found = findLabel(child, text);
        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  private JButton findButton(Component component) {
    if (component instanceof JButton button) {
      return button;
    }

    if (component instanceof JPanel panel) {
      for (Component child : panel.getComponents()) {
        JButton found = findButton(child);
        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }
}
