package todo.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles user registration and login.
 * All password hashing is done with BCrypt; plaintext passwords never leave this class.
 */
public class AuthService {

  private final UserRepository userRepository;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Registers a new user account.
   *
   * @param username 3–50 characters
   * @param email    must contain {@code @}
   * @param plainPassword at least 8 characters
   * @return the newly created {@link User}
   * @throws AuthException if validation fails or username/email is already taken
   */
  public User register(String username, String email, String plainPassword) {
    validateUsername(username);
    validateEmail(email);
    validatePassword(plainPassword);

    if (userRepository.findByUsername(username).isPresent()) {
      throw new AuthException("Username already taken.");
    }
    if (userRepository.findByEmail(email).isPresent()) {
      throw new AuthException("Email already registered.");
    }

    String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    userRepository.save(new User(0, username, hash, email));
    return userRepository.findByUsername(username).orElseThrow();
  }

  /**
   * Authenticates a user by username and password.
   *
   * @return the authenticated {@link User}
   * @throws AuthException with a generic message to avoid username enumeration
   */
  public User login(String username, String plainPassword) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new AuthException("Invalid username or password."));
    if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
      throw new AuthException("Invalid username or password.");
    }
    return user;
  }

  private void validateUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new AuthException("Username must not be blank.");
    }
    if (username.length() < 3 || username.length() > 50) {
      throw new AuthException("Username must be 3–50 characters.");
    }
  }

  private void validateEmail(String email) {
    if (email == null || !email.contains("@")) {
      throw new AuthException("Email must contain '@'.");
    }
  }

  private void validatePassword(String password) {
    if (password == null || password.length() < 8) {
      throw new AuthException("Password must be at least 8 characters.");
    }
  }
}
