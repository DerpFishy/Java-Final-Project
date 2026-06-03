## Project

CLI-based todo list app in Java 25, built with Maven (`mvn exec:java` to run, `mvn test` for tests).

**Package root:** `todo.*` under `src/main/java/`

**Architecture:**
- `task/` — `Task` (id, title, priority, dueDate, project, status), `TaskRepository` interface, `InMemoryTaskRepository` (tests), `FileTaskRepository` (persists to `tasks.dat` via Java serialization)
- `service/` — `TaskService` (CRUD, editTask, sort by date/project), `TaskStatisticsService` (completion count)
- `command/` — Command pattern: `AddTaskCommand`, `CompleteTaskCommand`, `EditTaskCommand`, `DeleteTaskCommand`
- `event/` — Pub/sub `EventBus`; events: `TaskCreatedEvent`, `TaskCompletedEvent`
- `ui/` — `ConsoleUI`: interactive Scanner menu loop, delegates all mutations to commands
- `util/` — `IdGenerator` (8-char UUID)
- `Main` — wires `FileTaskRepository` → `TaskService` → `ConsoleUI`, subscribes event log to `EventBus`

**Test:** `src/test/java/todo/service/TaskServiceTest` — 7 JUnit 5 tests (CRUD, edit partial-update, sort by date, sort by project, null-date ordering)

## Commits

- **Atomic commits:** one logical change per commit — feature, fix, refactor, test each get their own commit. Never bundle unrelated changes.
- **Conventional Commits format:** `type(scope): description` in imperative mood, e.g. `feat(ai): add hint generation`, `fix(core): handle ko edge case`. Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `perf`, `style`, `ci`.
- **Must pass before committing:** `mvn test` green, code compiles cleanly, no unrelated modified files staged, IntelliJ inspections via idea MCP show no warnings or errors.
- Never commit dead code, debug prints, commented-out blocks, or half-finished work.

## Code style

- **Strictly adhere to the Google Java Style Guide (2-space indent, 100-col limit, lowerCamelCase for methods, UPPER_SNAKE_CASE for constants).**
- Braces: K&R, never omit braces around single-statement `if`/`for`/`while`.
- One top-level class per file; filename matches the public class.
- Imports: no wildcards; ordered as `java.*`, `javax.*`, third-party, then project packages, with a blank line between groups.
- Javadoc on every public class and non-trivial public method.
- Prefer package-private visibility over `public` when a class is only used inside its package.
- No `null` returns from collections-shaped APIs — return empty collections or `Optional`.

## OOP Principles

**Single responsibility** — one class, one job. When a class grows, split it by concern rather than adding more methods. Example: `TaskSetGenerator` delegates calculation to `FrameSizeCalculator` and validation to `TaskSetValidator`.

**Stateless utilities** — group pure, stateless operations into a final utility class with a private constructor and `static` methods (e.g., `JsonIO`) instead of leaking logic into instances or mixed-bag classes.

**Template method via `parse()`** — `AppBaseModel.loadFromJson()` defines the loading skeleton, using a `protected abstract` method `parse()` that subclasses override for custom construction logic.

**Dependency Injection (DI)** — accept collaborators via constructor parameters, leveraging interface abstractions where possible. This keeps classes highly testable and loosely coupled without relying on reflection hacks or global state.

**Private by default** — use the `private` access modifier for all internal methods and fields. Use `protected` only when explicit subclass overriding is required, and expose public APIs strictly through targeted methods.