package todo.auth;

import java.util.Optional;

/** Persistence contract for {@link User} records. */
public interface UserRepository {

  /** Persists a new user. The {@code passwordHash} field must already be hashed. */
  void save(User user);

  /** Persists changes to an existing user (e.g. updated password hash or email). */
  void update(User user);

  /** Looks up a user by exact username match. */
  Optional<User> findByUsername(String username);

  /** Looks up a user by exact email match. */
  Optional<User> findByEmail(String email);
}
