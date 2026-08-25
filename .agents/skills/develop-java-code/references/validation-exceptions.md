# Validation and Exception Rules

## Validation

- **JAVA-VALIDATION-001** — Use Jakarta Bean Validation for constraints on bindable Bean properties and method parameters.
- **JAVA-VALIDATION-002** — Use explicit checks only when framework validation cannot express a constraint or an internal API outside the framework binding path, such as a domain constructor or factory, requires fast failure; when Spring Framework is already present, use `org.springframework.util.Assert`, but do not add Spring only for this purpose.

## Exceptions and logging

- **JAVA-EXCEPTION-001** — Use framework exception types for framework, binding, and validation failures; use explicit domain exceptions for invariants and business constraints.
- **JAVA-EXCEPTION-002** — Business exceptions must belong to a defined exception base or error-code hierarchy; generic runtime exceptions must not carry business semantics.
- **JAVA-EXCEPTION-003** — Handle or propagate business exceptions; log non-business exceptions with enough context to locate the problem, and never silently ignore them.
