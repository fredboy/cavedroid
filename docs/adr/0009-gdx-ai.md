# 0009 — AI via gdx-ai (behavior trees + FSM + steering)

- Status: Accepted
- Epics: S0, E4

## Context

1.x mob AI is bespoke `MobBehavior` subclasses (Passive/Aggressive/Archer/FallingBlock/Player) with hand-rolled LOS raycasts, autojump and tracking. Preference: fewer hand-rolled crutches, reuse the libGDX stack.

## Decision

Adopt the libGDX **`gdx-ai`** library for mob AI:

- **Behavior Trees + State Machines + steering** as the basis (replaces `MobBehavior`). AI instances live in ECS components, ticked in the `entityAI` phase.
- **A\* pathfinding** over the tile grid is a separate, possibly-**deferred** sub-task within E4 (nav graph over loaded chunks, time-sliced).
- Do **not** use gdx-ai `MessageDispatcher` — the command/event bus (ADR 0003) covers it.

Constraints:
- Drive `GdxAI.getTimepiece()` from `ClockContext` (not wall-clock).
- Route all AI randomness through the seeded `RandomSource` — else reconciliation (ADR 0005) diverges.
- AI emits actions via the command bus.
- Behavior-tree file loading uses reflection → verify on TeaVM as part of the S0 spike.

## Consequences

- Declarative, composable AI instead of class hierarchies.
- gdx-ai is stable but not actively developed — acceptable as a dependency.
- Global `GdxAI` singletons need careful per-session init under our DI/`SessionScope`.

## Spike result (S0, #131)

Verified on TeaVM/web (2026-06-01): a gdx-ai `BehaviorTree` builds and steps in the browser, and `ClassReflection.newInstance` works — so the reflective `.tree` parser is expected to work too. **Decision: GO.** Still to confirm during E4: the full `.tree` DSL parse and driving `GdxAI.getTimepiece()` from the deterministic tick clock (#148).
