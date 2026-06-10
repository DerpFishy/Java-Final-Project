package todo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import todo.auth.AuthException;
import todo.auth.AuthService;
import todo.auth.User;

/** Desktop login/register window shown before the main todo UI. */
public class DesktopLoginFrame extends JFrame {

  private final AuthService authService;
  private final java.util.function.Consumer<User> onSuccess;

  private final JTextField usernameField = new JTextField();
  private final JTextField emailField = new JTextField();
  private final JLabel emailLabel = new JLabel("Email:");
  private final JPasswordField passwordField = new JPasswordField();
  private final JLabel modeLabel = new JLabel("Current screen: Login", SwingConstants.CENTER);
  private final JLabel titleLabel = new JLabel("Log in to Todo", SwingConstants.CENTER);
  private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
  private final JButton primaryButton = new JButton("Log in");
  private final JButton secondaryButton = new JButton("Register");
  private boolean registerMode = false;

  public DesktopLoginFrame(AuthService authService, java.util.function.Consumer<User> onSuccess) {
    super("Todo Login");
    this.authService = authService;
    this.onSuccess = onSuccess;

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(500, 520));
    setPreferredSize(new Dimension(500, 520));
    setLocationRelativeTo(null);
    setContentPane(createContent());
    pack();
  }

  private JPanel createContent() {
    JPanel panel = new JPanel(new BorderLayout(12, 12));
    panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

    titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
    modeLabel.setForeground(new Color(40, 80, 140));

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Account"),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    int row = 0;
    addRow(formPanel, gbc, row++, "Username", usernameField);
    addRow(formPanel, gbc, row++, "Password", passwordField);
    addEmailRow(formPanel, gbc, row, emailLabel, emailField);

    emailLabel.setVisible(false);
    emailField.setVisible(false);
    emailField.setEnabled(false);

    statusLabel.setForeground(new Color(140, 0, 0));

    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    actionPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    JButton quitButton = new JButton("Quit");

    primaryButton.addActionListener(event -> {
      if (registerMode) {
        attemptRegister();
      } else {
        attemptLogin();
      }
    });
    secondaryButton.addActionListener(event -> {
      registerMode = !registerMode;
      updateModeUI();
    });
    quitButton.addActionListener(event -> System.exit(0));

    actionPanel.add(primaryButton);
    actionPanel.add(secondaryButton);
    actionPanel.add(quitButton);
    actionPanel.setOpaque(false);

    panel.add(modeLabel, BorderLayout.NORTH);
    panel.add(titleLabel, BorderLayout.PAGE_START);
    panel.add(formPanel, BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel(new BorderLayout(4, 4));
    bottomPanel.add(statusLabel, BorderLayout.NORTH);
    bottomPanel.add(actionPanel, BorderLayout.CENTER);
    panel.add(bottomPanel, BorderLayout.SOUTH);

    updateModeUI();

    return panel;
  }

  private void updateModeUI() {
    if (registerMode) {
      modeLabel.setText("Current screen: Register");
      titleLabel.setText("Create your account");
      emailLabel.setVisible(true);
      emailField.setVisible(true);
      emailField.setEnabled(true);
      primaryButton.setText("Register");
      secondaryButton.setText("Back to Login");
      statusLabel.setText("Register mode is active.");
    } else {
      modeLabel.setText("Current screen: Login");
      titleLabel.setText("Log in to Todo");
      emailLabel.setVisible(false);
      emailField.setVisible(false);
      emailField.setEnabled(false);
      primaryButton.setText("Log in");
      secondaryButton.setText("Register");
      statusLabel.setText("Login mode is active.");
    }
    revalidate();
    repaint();
  }

  private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText,
      JTextField field) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel(labelText + ":"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    field.setPreferredSize(new Dimension(220, 28));
    panel.add(field, gbc);
  }

  private void addEmailRow(JPanel panel, GridBagConstraints gbc, int row,
      JLabel label, JTextField field) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(label, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    field.setPreferredSize(new Dimension(220, 28));
    panel.add(field, gbc);
  }

  private void attemptLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isBlank() || password.isBlank()) {
      showStatus("Enter your username and password.");
      return;
    }

    try {
      User user = authService.login(username, password);
      showStatus("Welcome back, " + user.getUsername() + "!");
      dispose();
      onSuccess.accept(user);
    } catch (AuthException exception) {
      showStatus(exception.getMessage());
    }
  }

  private void attemptRegister() {
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isBlank() || email.isBlank() || password.isBlank()) {
      showStatus("Fill in username, email, and password to register.");
      return;
    }

    try {
      User user = authService.register(username, email, password);
      showStatus("Account created for " + user.getUsername() + ". Please log in.");
      usernameField.setText("");
      emailField.setText("");
      passwordField.setText("");
      registerMode = false;
      updateModeUI();
      return;
    } catch (AuthException exception) {
      showStatus(exception.getMessage());
    }
  }

  private void showStatus(String message) {
    statusLabel.setText(message);
  }
}
