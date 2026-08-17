# Distributed Lock Rules

## Correctness and execution

- **LOCK-DEFENSE-001** — A distributed lock is never the sole correctness guarantee; handle expiry, pauses, partitions, and duplicate execution and use the independent correctness protection defined by the persistence concurrency rule.
- **LOCK-PORT-001** — Use the injected `LockExecutor` and `LockContext(scene, key, waitTime)`; business code must not depend directly on Redisson, Spring Integration Redis Lock, or raw Redis lock commands.
- **LOCK-KEY-001** — Use a stable low-cardinality scene and a canonical business-unique resource key; never use random values, sensitive data, or multiple formats for the same resource.
- **LOCK-WAIT-001** — Bound lock wait time by the use-case SLA, prefer immediate acquisition, and return an explicit retry/conflict/degradation result on failure; never wait indefinitely or spin without backoff.
- **LOCK-LEASE-001** — Configure a lease longer than the normal critical-section bound, keep critical sections short, and avoid remote calls, bulk work, and long transactions even when automatic renewal is enabled.
- **LOCK-RELEASE-001** — Use `LockExecutor`-managed execution so only the owning flow unlocks in `finally`; do not transfer lock ownership across threads.
