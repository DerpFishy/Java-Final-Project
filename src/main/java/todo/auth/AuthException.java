package todo.auth;

/** Thrown when a registration or login operation fails. */
public class AuthException extends RuntimeException {

  public AuthException(String message) {
    super(message);
  }
}
