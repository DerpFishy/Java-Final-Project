package todo.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import todo.task.Priority;
import todo.task.Task;

/** Dialog for collecting fields needed to create or edit a task. */
public final class TaskDialog {

  private TaskDialog() {
  }

  /** Shows a task form and returns its values, or {@code null} when cancelled. */
  public static TaskFormData show(Component parent, LocalDate suggestedDate, Task task) {
    JTextField titleField = new JTextField(24);
    JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    JTextField dateField = new JTextField(12);
    JTextField projectField = new JTextField(24);

    if (task == null) {
      priorityBox.setSelectedItem(Priority.MEDIUM);
      dateField.setText(suggestedDate == null ? "" : suggestedDate.toString());
    } else {
      titleField.setText(task.getTitle());
      priorityBox.setSelectedItem(task.getPriority());
      dateField.setText(task.getDueDate() == null ? "" : task.getDueDate().toString());
      projectField.setText(task.getProject() == null ? "" : task.getProject());
    }

    JPanel fields = createFieldsPanel(titleField, priorityBox, dateField, projectField);
    String dialogTitle = task == null
        ? text("Add Task", "新增任務")
        : text("Edit Task", "編輯任務");

    while (true) {
      int result = JOptionPane.showConfirmDialog(
          parent,
          fields,
          dialogTitle,
          JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.PLAIN_MESSAGE);
      if (result != JOptionPane.OK_OPTION) {
        return null;
      }

      String title = titleField.getText().trim();
      if (title.isEmpty()) {
        showValidationError(parent, text("Title is required.", "標題為必填。"));
        continue;
      }

      try {
        LocalDate dueDate = parseDate(dateField.getText());
        String project = projectField.getText().trim();
        return new TaskFormData(
            title,
            (Priority) priorityBox.getSelectedItem(),
            dueDate,
            project.isEmpty() ? null : project);
      } catch (DateTimeParseException exception) {
        showValidationError(
            parent,
            text("Due date must use yyyy-mm-dd format.", "到期日必須使用 yyyy-mm-dd 格式。"));
      }
    }
  }

  private static JPanel createFieldsPanel(
      JTextField titleField,
      JComboBox<Priority> priorityBox,
      JTextField dateField,
      JTextField projectField) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JPanel fields = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    addField(fields, constraints, 0, text("Title", "標題"), titleField);
    addField(fields, constraints, 1, text("Priority", "優先度"), priorityBox);
    addField(fields, constraints, 2, text("Due date", "到期日"), dateField);
    addField(fields, constraints, 3, text("Project", "專案"), projectField);

    JLabel dateHint = new JLabel(
        text("Use yyyy-mm-dd; leave blank for no due date.",
            "請使用 yyyy-mm-dd；留空表示無到期日。"));
    constraints.gridx = 1;
    constraints.gridy = 4;
    constraints.weightx = 1;
    fields.add(dateHint, constraints);
    panel.add(fields, BorderLayout.CENTER);
    return panel;
  }

  private static void addField(
      JPanel panel,
      GridBagConstraints constraints,
      int row,
      String label,
      Component component) {
    constraints.gridx = 0;
    constraints.gridy = row;
    constraints.weightx = 0;
    panel.add(new JLabel(label + ":"), constraints);

    constraints.gridx = 1;
    constraints.weightx = 1;
    panel.add(component, constraints);
  }

  private static LocalDate parseDate(String value) {
    String normalized = value.trim();
    return normalized.isEmpty() ? null : LocalDate.parse(normalized);
  }

  private static void showValidationError(Component parent, String message) {
    JOptionPane.showMessageDialog(
        parent,
        message,
        text("Invalid Task", "任務資料無效"),
        JOptionPane.ERROR_MESSAGE);
  }

  private static String text(String english, String traditionalChinese) {
    return Locale.getDefault().getLanguage().startsWith("zh")
        ? traditionalChinese
        : english;
  }

  /** Values submitted from the task form. */
  public record TaskFormData(
      String title, Priority priority, LocalDate dueDate, String project) {
  }
}
