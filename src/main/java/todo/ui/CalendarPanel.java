package todo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import todo.task.Task;
import todo.task.TaskStatus;

/** Month calendar that displays task counts and reports the selected date. */
public class CalendarPanel extends JPanel {

  private static final Color SELECTED_COLOR = new Color(52, 120, 246);
  private static final Color TODAY_COLOR = new Color(220, 235, 255);
  private static final Color OUTSIDE_MONTH_COLOR = new Color(245, 245, 245);

  private static final int DAY_BUTTON_FONT_SIZE = 14;

  private final Consumer<LocalDate> dateSelectionListener;
  private final JLabel monthLabel;
  private final JLabel ongoingLabel;
  private final JLabel overdueLabel;
  private final JLabel completedLabel;
  private final JPanel daysPanel;

  private YearMonth displayedMonth;
  private LocalDate selectedDate;
  private List<Task> tasks;

  public CalendarPanel(Consumer<LocalDate> dateSelectionListener) {
    this.dateSelectionListener = dateSelectionListener;
    this.displayedMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();
    this.tasks = List.of();

    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JButton previousButton = new JButton("<");
    previousButton.setFont(previousButton.getFont().deriveFont(Font.BOLD, 13f));
    previousButton.setToolTipText("Previous month");
    previousButton.addActionListener(event -> changeMonth(-1));

    JButton nextButton = new JButton(">");
    nextButton.setFont(nextButton.getFont().deriveFont(Font.BOLD, 13f));
    nextButton.setToolTipText("Next month");
    nextButton.addActionListener(event -> changeMonth(1));

    JButton todayButton = new JButton("Today");
    todayButton.setFont(todayButton.getFont().deriveFont(Font.PLAIN, 13f));
    todayButton.addActionListener(event -> selectDate(LocalDate.now()));

    monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 18f));

    ongoingLabel = new JLabel("Ongoing tasks: 0", SwingConstants.CENTER);
    overdueLabel = new JLabel("Overdue tasks: 0", SwingConstants.CENTER);
    completedLabel = new JLabel("Completed tasks: 0", SwingConstants.CENTER);
    ongoingLabel.setFont(ongoingLabel.getFont().deriveFont(Font.PLAIN, 13f));
    overdueLabel.setFont(overdueLabel.getFont().deriveFont(Font.PLAIN, 13f));
    completedLabel.setFont(completedLabel.getFont().deriveFont(Font.PLAIN, 13f));

    JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    summaryPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    summaryPanel.add(ongoingLabel);
    summaryPanel.add(overdueLabel);
    summaryPanel.add(completedLabel);

    JPanel navigationPanel = new JPanel(new BorderLayout(8, 0));
    navigationPanel.add(previousButton, BorderLayout.WEST);
    navigationPanel.add(monthLabel, BorderLayout.CENTER);

    JPanel navigationActions = new JPanel(new GridLayout(1, 2, 6, 0));
    navigationActions.add(todayButton);
    navigationActions.add(nextButton);
    navigationPanel.add(navigationActions, BorderLayout.EAST);

    JPanel topPanel = new JPanel(new BorderLayout(4, 4));
    topPanel.add(navigationPanel, BorderLayout.NORTH);
    topPanel.add(summaryPanel, BorderLayout.SOUTH);
    add(topPanel, BorderLayout.NORTH);

    daysPanel = new JPanel(new GridLayout(7, 7, 4, 4));
    daysPanel.setPreferredSize(new Dimension(630, 500));
    add(daysPanel, BorderLayout.CENTER);

    rebuildCalendar();
  }

  /** Replaces the tasks used to calculate each date's task count. */
  public void setTasks(List<Task> tasks) {
    this.tasks = List.copyOf(tasks);
    rebuildCalendar();
  }

  /** Selects a date and changes the displayed month when necessary. */
  public void selectDate(LocalDate date) {
    selectedDate = date;
    displayedMonth = YearMonth.from(date);
    rebuildCalendar();
    dateSelectionListener.accept(date);
  }

  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  private void changeMonth(long months) {
    displayedMonth = displayedMonth.plusMonths(months);
    selectedDate = displayedMonth.atDay(1);
    rebuildCalendar();
    dateSelectionListener.accept(selectedDate);
  }

  private void rebuildCalendar() {
    daysPanel.removeAll();
    monthLabel.setText(displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
        + " " + displayedMonth.getYear());

    updateSummaryLabels();
    addDayHeaders();
    Map<LocalDate, Integer> taskCounts = countTasksByDate();
    LocalDate gridStart = displayedMonth.atDay(1)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

    for (int offset = 0; offset < 42; offset++) {
      LocalDate date = gridStart.plusDays(offset);
      daysPanel.add(createDayButton(date, taskCounts.getOrDefault(date, 0)));
    }

    daysPanel.revalidate();
    daysPanel.repaint();
  }

  private void updateSummaryLabels() {
    int ongoingCount = 0;
    int overdueCount = 0;
    int completedCount = 0;

    for (Task task : tasks) {
      if (task.getStatus() == TaskStatus.COMPLETED) {
        completedCount++;
      } else if (TaskAppearancePalette.isOverdue(task)) {
        overdueCount++;
      } else {
        ongoingCount++;
      }
    }

    ongoingLabel.setText("Ongoing tasks: " + ongoingCount);
    overdueLabel.setText("Overdue tasks: " + overdueCount);
    completedLabel.setText("Completed tasks: " + completedCount);
  }

  private void addDayHeaders() {
    for (DayOfWeek day : orderedDays()) {
      JLabel label = new JLabel(
          day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
          SwingConstants.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD));
      daysPanel.add(label);
    }
  }

  private DayOfWeek[] orderedDays() {
    return new DayOfWeek[] {
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    };
  }

  private Map<LocalDate, Integer> countTasksByDate() {
    Map<LocalDate, Integer> counts = new HashMap<>();
    for (Task task : tasks) {
      if (task.getDueDate() != null) {
        counts.merge(task.getDueDate(), 1, Integer::sum);
      }
    }
    return counts;
  }

  private JButton createDayButton(LocalDate date, int taskCount) {
    String countText = taskCount == 0 ? "" : "<br><small>" + taskCount + " task"
        + (taskCount == 1 ? "" : "s") + "</small>";
    JButton button = new JButton("<html><center>" + date.getDayOfMonth() + countText
        + "</center></html>");
    button.setHorizontalAlignment(SwingConstants.CENTER);
    button.setVerticalAlignment(SwingConstants.TOP);
    button.setMargin(new Insets(6, 6, 6, 6));
    button.setFont(button.getFont().deriveFont(Font.PLAIN, DAY_BUTTON_FONT_SIZE));
    button.setBorder(BorderFactory.createLineBorder(new Color(215, 215, 215), 1));
    button.setOpaque(true);
    button.setFocusPainted(false);

    boolean inDisplayedMonth = YearMonth.from(date).equals(displayedMonth);
    if (!inDisplayedMonth) {
      button.setBackground(OUTSIDE_MONTH_COLOR);
      button.setForeground(Color.GRAY);
    } else if (date.equals(LocalDate.now())) {
      button.setBackground(TODAY_COLOR);
    } else {
      button.setBackground(Color.WHITE);
    }

    if (date.equals(selectedDate)) {
      button.setBackground(SELECTED_COLOR);
      button.setForeground(Color.WHITE);
      button.setBorder(BorderFactory.createLineBorder(new Color(30, 92, 210), 2));
      button.setFont(button.getFont().deriveFont(Font.BOLD, DAY_BUTTON_FONT_SIZE));
    }

    button.addActionListener(event -> selectDate(date));
    return button;
  }
}
