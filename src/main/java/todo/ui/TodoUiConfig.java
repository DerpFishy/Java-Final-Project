package todo.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Stores user-facing UI preferences for the desktop todo calendar. */
public final class TodoUiConfig {

  private static final Path CONFIG_FILE =
      Path.of(System.getProperty("user.home"), ".todo-calendar.properties");
  private static final String WARNING_DAYS_KEY = "ui.warningDaysBeforeDueDate";

  private final int warningDaysBeforeDueDate;

  public TodoUiConfig(int warningDaysBeforeDueDate) {
    this.warningDaysBeforeDueDate = clampWarningDays(warningDaysBeforeDueDate);
  }

  public static TodoUiConfig load() {
    if (!Files.exists(CONFIG_FILE)) {
      return loadFromDefaults();
    }

    Properties properties = new Properties();
    try (InputStream inputStream = Files.newInputStream(CONFIG_FILE)) {
      properties.load(inputStream);
    } catch (IOException exception) {
      return loadFromDefaults();
    }

    int warningDays = parseInt(properties.getProperty(WARNING_DAYS_KEY, "3"));
    return new TodoUiConfig(warningDays);
  }

  public static TodoUiConfig loadFromDefaults() {
    return new TodoUiConfig(3);
  }

  public void save() {
    Properties properties = new Properties();
    properties.setProperty(WARNING_DAYS_KEY, Integer.toString(warningDaysBeforeDueDate));

    try {
      Files.createDirectories(CONFIG_FILE.getParent());
      try (OutputStream outputStream = Files.newOutputStream(CONFIG_FILE)) {
        properties.store(outputStream, "Todo Calendar UI Settings");
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to save UI settings", exception);
    }
  }

  public int getWarningDaysBeforeDueDate() {
    return warningDaysBeforeDueDate;
  }

  private static int clampWarningDays(int warningDaysBeforeDueDate) {
    return Math.max(0, Math.min(30, warningDaysBeforeDueDate));
  }

  private static int parseInt(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
      return 3;
    }
  }
}
