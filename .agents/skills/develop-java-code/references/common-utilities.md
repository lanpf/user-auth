# Common Utility Rules

## Selection and reuse

- **JAVA-UTILITY-001** — Prefer the JDK, an already-used framework, or a mature maintained library over reimplementing general technical capabilities.
- **JAVA-UTILITY-002** — When a module already depends on Spring Framework for its responsibility, use `org.springframework.util.StringUtils.hasText`, `CollectionUtils.isEmpty`, and matching Spring utilities. A Spring-decoupled module must not add Spring only for utilities; when the JDK is insufficient, use project-managed Apache Commons `StringUtils.isBlank`/`isNotBlank` and `CollectionUtils.isEmpty`/`isNotEmpty`. Do not hand-write equivalents already provided by these utilities.
- **JAVA-UTILITY-003** — Before adding a library, evaluate maintenance activity, security, license, and dependency cost; do not add overlapping functionality when existing dependencies suffice.
