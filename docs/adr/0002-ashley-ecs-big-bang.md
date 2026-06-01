# 0002 — Ashley ECS; big-bang refactor shipped as 2.0

- Status: Accepted
- Epics: S0, E1, E4

## Context

1.x uses a bespoke entity system: `Mob`/`Player`/`Drop`/`Projectile` classes with their own `update()`, driven by `MobController`/`DropController`/`ProjectileController` over `LinkedList`s, fed the world through `*WorldAdapter` interfaces and spawned via `*Queue`s. Entities are "smart objects" that pull the world in to update themselves — the source of the adapter explosion.

## Decision

Adopt **Ashley ECS**. Entities become compositions of components; controllers/adapters/queues are replaced by **systems** + an `EntityFactory`. Movement/AI/physics-sync/damage/etc. become systems that hold the world and components directly (no inversion, no adapters).

The migration is **big-bang, not incremental** — no intermediate half-measures. It lands as release **2.0** on a dedicated `refactor/2.0` branch; the stable line stays available for hotfixes. Approach is **ECS-first**: build `engine:ecs` + Box2D/render bridges, then port all entities at once.

Gating risk: Ashley uses reflection — verify it runs under TeaVM/web first (S0).

## Consequences

- `*WorldAdapter`, controllers and spawn queues are deleted.
- Logic moves into pure, testable systems.
- No always-shippable intermediate states; correctness is validated by tests + the parity suite before 2.0.

## Spike result (S0, #131)

Verified on TeaVM/web (`html:runWeb`, 2026-06-01): an Ashley `Engine` + `ComponentMapper` + `Family` + `IteratingSystem` run in the browser, and libGDX `ClassReflection.newInstance` (the reflection Ashley relies on) works. **Decision: GO** — no array-backed fallback ECS needed.
