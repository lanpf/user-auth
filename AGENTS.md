<!-- engineering-standards:begin version=2.1.6 -->
## Shared engineering guidance

- **STD-HIERARCHY-001** — Shared rules apply to every project; service rules additionally apply to service projects, and project-specific authoritative documents may only add or tighten constraints.
- **STD-DOCS-001** — Before changing a service, read the project documentation entry declared by its project guidance, then read every authoritative document routed for the task.
- **STD-DOMAIN-002** — Before changing domain boundaries, language, business rules, errors, domain events, or API business semantics, read the service's authoritative domain document.
- **JAVA-VERSION-001** — Use Java 17.
- **DEP-CYCLE-001** — Module dependency cycles are forbidden; fix responsibilities or extract a shared module rather than hiding a cycle through reflection or events.
- **ARCH-DIRECTION-001** — Inner modules must not depend on outer protocols, persistence implementations, persistence objects, or the boot module.
- **ARCH-RESPONSIBILITY-001** — Keep domain rules in domain, use-case orchestration in application, technical adapters in infrastructure, protocol handling in interfaces, and runtime assembly in boot.
- **ERROR-STABILITY-001** — Never modify, reuse, or reassign a published error code.
- **VERIFY-CHANGE-001** — Run verification proportional to the change and do not claim completion without reporting the commands and results.

## Skill routing

- Developing, changing, or reviewing service code under the shared engineering standards during development: use `$develop-service-code`.
- Implementing or reviewing cross-instance locking, reliable event publication, message consumption, partitioning, dead letters, delayed messages, or related failure handling.: use `$develop-distributed-capabilities`.
- Designing, implementing, or reviewing compensation, reconciliation, repair, cleanup, batch recovery, or fallback workflows: use `$develop-compensation-workflows`.
- Writing, running, or reviewing integration tests in the smoke-test phase: use `$test-integration`.
- Planning or applying coordinated standards-driven layered service refactors: use `$refactor-layered-service`.

<!-- engineering-standards:end -->

## Project documentation

- Before making any change, read the root `README.md` for the project scope, module map, verification commands, and documentation routing.
- Before changing domain models, invariants, business rules, application use cases, API business semantics, error definitions, or domain events, also read `docs/DOMAIN.md`.
- Before changing authorization-server providers, SAS or OAuth2/OIDC behavior, grants, token lifecycle, scopes, internal clients, protocol endpoint security, JWKs, protocol persistence, or authorization-server configuration, also read `docs/OAUTH2_OIDC_PROTOCOL_INFRASTRUCTURE.md`.
- Read every additional authoritative document routed by the root `README.md` that applies to the task.
- If a change crosses multiple documented areas, read all applicable authoritative documents.
- The root `README.md` is the project entry and documentation index; the routed detailed documents are authoritative within their respective scopes.
