package todo.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** Stores user-facing UI preferences for the desktop todo calendar. */
public final class TodoUiConfig {

  private static final Path CONFIG_FILE =
      Path.of(System.getProperty("user.home"), ".todo-calendar.properties");
  private static final String WARNING_DAYS_KEY = "ui.warningDaysBeforeDueDate";
  private static final String LANGUAGE_KEY = "ui.language";
  private static final String DEFAULT_LANGUAGE_CODE = "en";

  private final int warningDaysBeforeDueDate;
  private final String languageCode;

  public TodoUiConfig(int warningDaysBeforeDueDate) {
    this(warningDaysBeforeDueDate, DEFAULT_LANGUAGE_CODE);
  }

  public TodoUiConfig(int warningDaysBeforeDueDate, String languageCode) {
    this.warningDaysBeforeDueDate = clampWarningDays(warningDaysBeforeDueDate);
    this.languageCode = normalizeLanguageCode(languageCode);
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
    String languageCode = properties.getProperty(LANGUAGE_KEY, detectDefaultLanguageCode());
    return new TodoUiConfig(warningDays, languageCode);
  }

  public static TodoUiConfig loadFromDefaults() {
    return new TodoUiConfig(3, detectDefaultLanguageCode());
  }

  public void save() {
    Properties properties = new Properties();
    properties.setProperty(WARNING_DAYS_KEY, Integer.toString(warningDaysBeforeDueDate));
    properties.setProperty(LANGUAGE_KEY, languageCode);

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

  public String getLanguageCode() {
    return languageCode;
  }

  public java.util.Locale getLocale() {
    return switch (languageCode) {
      case "zh_TW", "zh-TW", "zh-Hant-TW" -> java.util.Locale.forLanguageTag("zh-TW");
      default -> java.util.Locale.ENGLISH;
    };
  }

  private static int clampWarningDays(int warningDaysBeforeDueDate) {
    return Math.max(0, Math.min(30, warningDaysBeforeDueDate));
  }

  private static String detectDefaultLanguageCode() {
    Locale systemLocale = Locale.getDefault();
    return systemLocale.getLanguage().startsWith("zh") ? "zh_TW" : "en";
  }

  private static int parseInt(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
      return 3;
    }
  }

  private static String normalizeLanguageCode(String languageCode) {
    if (languageCode == null) {
      return DEFAULT_LANGUAGE_CODE;
    }

    String normalized = languageCode.trim();
    if (normalized.equalsIgnoreCase("zh_TW")
        || normalized.equalsIgnoreCase("zh-TW")
        || normalized.equalsIgnoreCase("zh-Hant-TW")
        || normalized.equalsIgnoreCase("traditional")
        || normalized.equalsIgnoreCase("taiwan")) {
      return "zh_TW";
    }

    return "en";
  }
}
