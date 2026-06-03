package todo.auth;

/** Represents a registered user account. */
public class User {

  private final int id;
  private final String username;
  private String passwordHash;
  private String email;

  public User(int id, String username, String passwordHash, String email) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.email = email;
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  /** Updates the stored password hash (e.g. after a password reset). */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getEmail() {
    return email;
  }

  /** Updates the stored email address. */
  public void setEmail(String email) {
    this.email = email;
  }
}
