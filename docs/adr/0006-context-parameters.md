# 0006 — Kotlin context parameters for ambient contexts

- Status: Accepted
- Epics: E2, E4

## Context

1.x threads the world into entities through fragmented adapter interfaces (`MobWorldAdapter`, `DropWorldAdapter`, `ProjectileWorldAdapter`, `ContainerWorldAdapter`) and param soup like `update(mobWorldAdapter, playerAdapter, projectileAdapter, delta)`.

## Decision

Use **Kotlin context parameters** for cross-cutting ambient dependencies, established once at the top of the tick:

- `WorldContext` — read + pure checks over the tile world.
- `CommandContext` — write-only; the single mutation path (`submit`).
- `ClockContext` — `gameTick`, `partialTick`, seeded `RandomSource`.
- `EcsContext` — engine/entity factory access.
- `ContentRegistry` — content + capabilities.

Systems and actions declare the contexts they need and reference them implicitly. Stable collaborators (asset repos, sound) stay constructor-injected via Dagger.

## Consequences

- Adapters and param soup disappear; actions read like `context(world, commands, clock) fun perform(...)`.
- Writes are forced through `CommandContext` (aligns with ADR 0003/0005).
- Context parameters are stabilizing in Kotlin 2.2 — keep behind the compiler flag, do not overuse beyond ambient deps; they do not auto-cross suspend boundaries.
