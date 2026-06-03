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
