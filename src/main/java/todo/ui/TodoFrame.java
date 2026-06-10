package todo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import todo.command.AddTaskCommand;
import todo.command.CompleteTaskCommand;
import todo.command.DeleteTaskCommand;
import todo.command.EditTaskCommand;
import todo.service.TaskService;
import todo.task.Priority;
import todo.task.Task;
import todo.task.TaskStatus;
import todo.ui.TaskDialog.TaskFormData;

/** Main desktop window for viewing and managing tasks through a calendar. */
public class TodoFrame extends JFrame {

  private static final DateTimeFormatter DATE_HEADING =
      DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);

  private final TaskService service;
  private final CalendarPanel calendarPanel;
  private final DefaultListModel<Task> taskListModel;
  private final JList<Task> taskList;
  private final JLabel taskHeading;
  private final JTextField searchField;
  private final JComboBox<Object> priorityFilterBox;
  private final JCheckBox showAllCheckBox;
  private final JButton editButton;
  private final JButton completeButton;
  private final JButton deleteButton;
  private TodoUiConfig config;

  public TodoFrame(TaskService service) {
    this(service, TodoUiConfig.load());
  }

  public TodoFrame(TaskService service, TodoUiConfig config) {
    super("Todo Calendar");
    this.service = service;
    this.config = config;

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(1000, 650));

    calendarPanel = new CalendarPanel(this::handleDateSelected);
    taskListModel = new DefaultListModel<>();
    taskList = new JList<>(taskListModel);
    taskHeading = new JLabel();
    searchField = new JTextField();
    priorityFilterBox = new JComboBox<>(new Object[] {
        "All priorities", Priority.LOW, Priority.MEDIUM, Priority.HIGH
    });
    showAllCheckBox = new JCheckBox("Show all tasks");
    editButton = new JButton("Edit");
    completeButton = new JButton("Complete");
    deleteButton = new JButton("Delete");

    configureTaskList();
    setContentPane(createContent());
    refreshTasks();

    pack();
    setLocationRelativeTo(null);
  }

  private JSplitPane createContent() {
    JPanel taskPanel = createTaskPanel();
    JSplitPane splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, calendarPanel, taskPanel);
    splitPane.setResizeWeight(0.68);
    splitPane.setDividerLocation(680);
    splitPane.setBorder(null);
    return splitPane;
  }

  private JPanel createTaskPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 12));
    panel.setPreferredSize(new Dimension(360, 600));

    taskHeading.setFont(taskHeading.getFont().deriveFont(Font.BOLD, 17f));

    JPanel headingPanel = new JPanel(new BorderLayout(6, 6));
    headingPanel.add(taskHeading, BorderLayout.NORTH);

    JPanel searchPanel = new JPanel(new BorderLayout(6, 0));
    searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
    searchField.setToolTipText("Search by title or project");
    searchField.putClientProperty("JTextField.placeholderText", "Type a keyword");
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        refreshTaskList();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        refreshTaskList();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        refreshTaskList();
      }
    });
    searchPanel.add(searchField, BorderLayout.CENTER);

    JButton clearSearchButton = new JButton("Clear");
    clearSearchButton.addActionListener(event -> {
      searchField.setText("");
      refreshTaskList();
    });
    searchPanel.add(clearSearchButton, BorderLayout.EAST);

    JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    filterPanel.add(new JLabel("Priority:"));
    filterPanel.add(priorityFilterBox);
    filterPanel.add(showAllCheckBox);

    priorityFilterBox.addActionListener(event -> refreshTaskList());
    showAllCheckBox.addActionListener(event -> refreshTaskList());

    headingPanel.add(searchPanel, BorderLayout.CENTER);
    headingPanel.add(filterPanel, BorderLayout.SOUTH);
    panel.add(headingPanel, BorderLayout.NORTH);

    panel.add(new JScrollPane(taskList), BorderLayout.CENTER);

    JButton addButton = new JButton("Add");
    JButton settingsButton = new JButton("Settings");
    addButton.addActionListener(event -> addTask());
    settingsButton.addActionListener(event -> openSettingsDialog());
    editButton.addActionListener(event -> editSelectedTask());
    completeButton.addActionListener(event -> completeSelectedTask());
    deleteButton.addActionListener(event -> deleteSelectedTask());

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    actions.add(settingsButton);
    actions.add(addButton);
    actions.add(editButton);
    actions.add(completeButton);
    actions.add(deleteButton);
    panel.add(actions, BorderLayout.SOUTH);
    updateActionState();
    return panel;
  }

  private void configureTaskList() {
    taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    taskList.setCellRenderer(new TaskCellRenderer());
    taskList.setFixedCellHeight(58);
    taskList.addListSelectionListener(event -> updateActionState());
    taskList.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent event) {
        if (event.getClickCount() == 2 && taskList.getSelectedValue() != null) {
          editSelectedTask();
        }
      }
    });
  }

  private void handleDateSelected(LocalDate date) {
    refreshTaskList();
  }

  private void refreshTasks() {
    calendarPanel.setTasks(service.getTasks());
    refreshTaskList();
  }

  private void refreshTaskList() {
    LocalDate selectedDate = calendarPanel.getSelectedDate();
    Priority selectedPriority = priorityFilterBox.getSelectedItem() instanceof Priority
        ? (Priority) priorityFilterBox.getSelectedItem()
        : null;

    List<Task> visibleTasks = filterVisibleTasks(
        service.getTasks(),
        searchField.getText(),
        selectedDate,
        showAllCheckBox.isSelected(),
        selectedPriority);

    taskListModel.clear();
    visibleTasks.forEach(taskListModel::addElement);

    if (showAllCheckBox.isSelected()) {
      taskHeading.setText("All Tasks (" + visibleTasks.size() + ")");
    } else {
      taskHeading.setText(DATE_HEADING.format(selectedDate)
          + " (" + visibleTasks.size() + ")");
    }
    updateActionState();
  }

  static List<Task> filterVisibleTasks(
      List<Task> tasks,
      String keyword,
      LocalDate selectedDate,
      boolean showAll) {
    return filterVisibleTasks(tasks, keyword, selectedDate, showAll, null);
  }

  static List<Task> filterVisibleTasks(
      List<Task> tasks,
      String keyword,
      LocalDate selectedDate,
      boolean showAll,
      Priority selectedPriority) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

    return tasks.stream()
        .filter(task -> normalizedKeyword.isBlank()
            || containsKeyword(task.getTitle(), normalizedKeyword)
            || containsKeyword(task.getProject(), normalizedKeyword))
        .filter(task -> selectedPriority == null || task.getPriority() == selectedPriority)
        .filter(task -> showAll || selectedDate.equals(task.getDueDate()))
        .sorted(Comparator.comparing(
            Task::getDueDate,
            Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Task::getPriority, Comparator.reverseOrder())
            .thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  private static boolean containsKeyword(String value, String normalizedKeyword) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
  }

  private void addTask() {
    TaskFormData data = TaskDialog.show(this, calendarPanel.getSelectedDate(), null);
    if (data == null) {
      return;
    }

    new AddTaskCommand(
        service, data.title(), data.priority(), data.dueDate(), data.project()).execute();
    refreshTasks();
  }

  private void editSelectedTask() {
    Task task = taskList.getSelectedValue();
    if (task == null) {
      return;
    }

    TaskFormData data = TaskDialog.show(this, task.getDueDate(), task);
    if (data == null) {
      return;
    }

    new EditTaskCommand(
        service,
        task.getId(),
        data.title(),
        data.priority(),
        data.dueDate(),
        data.project())
        .execute();
    refreshTasks();
  }

  private void completeSelectedTask() {
    Task task = taskList.getSelectedValue();
    if (task == null || task.getStatus() == TaskStatus.COMPLETED) {
      return;
    }

    new CompleteTaskCommand(service, task.getId()).execute();
    refreshTasks();
  }

  private void deleteSelectedTask() {
    Task task = taskList.getSelectedValue();
    if (task == null) {
      return;
    }

    int choice = JOptionPane.showConfirmDialog(
        this,
        "Delete \"" + task.getTitle() + "\"?",
        "Delete Task",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    if (choice == JOptionPane.YES_OPTION) {
      new DeleteTaskCommand(service, task.getId()).execute();
      refreshTasks();
    }
  }

  private void updateActionState() {
    Task selectedTask = taskList.getSelectedValue();
    boolean hasSelection = selectedTask != null;
    editButton.setEnabled(hasSelection);
    deleteButton.setEnabled(hasSelection);
    completeButton.setEnabled(
        hasSelection && selectedTask.getStatus() != TaskStatus.COMPLETED);
  }

  private void openSettingsDialog() {
    JSpinner warningDaysSpinner = new JSpinner(
        new SpinnerNumberModel(config.getWarningDaysBeforeDueDate(), 0, 30, 1));

    JPanel settingsPanel = new JPanel(new BorderLayout(6, 6));
    settingsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JPanel rows = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
    rows.add(new JLabel("Highlight tasks due within this many days:"));
    rows.add(warningDaysSpinner);
    settingsPanel.add(rows, BorderLayout.CENTER);

    int choice = JOptionPane.showConfirmDialog(
        this,
        settingsPanel,
        "UI Settings",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE);

    if (choice == JOptionPane.OK_OPTION) {
      TodoUiConfig updatedConfig = new TodoUiConfig(
          ((Number) warningDaysSpinner.getValue()).intValue());
      updatedConfig.save();
      config = updatedConfig;
      refreshTasks();
    }
  }

  private final class TaskCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
        JList<?> list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      Task task = (Task) value;
      String project = task.getProject() == null ? "No project" : task.getProject();
      String dueDate = task.getDueDate() == null ? "No due date" : task.getDueDate().toString();
      String statusPrefix = task.getStatus() == TaskStatus.COMPLETED
          ? "[Done] "
          : TaskAppearancePalette.isOverdue(task) ? "[Overdue] " : "";
      Color backgroundColor = TaskAppearancePalette.getStatusColor(task, config);

      label.setText("<html><b>" + escape(statusPrefix + task.getTitle()) + "</b><br>"
          + escape(task.getPriority() + " | " + dueDate + " | " + project) + "</html>");
      label.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));

      if (isSelected) {
        label.setBackground(list.getSelectionBackground());
        label.setForeground(list.getSelectionForeground());
      } else {
        label.setBackground(backgroundColor);
        label.setForeground(TaskAppearancePalette.getLabelColor(task, false, config));
      }
      label.setOpaque(true);
      return label;
    }

    private String escape(String value) {
      return value
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;");
    }
  }
}
