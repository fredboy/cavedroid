# 0003 — Single-writer simulation + command bus on coroutines

- Status: Accepted
- Epics: E2

## Context

1.x world logic (`GameWorldFluidsLogicControllerTask`, grow blocks, fire, mob spawn/damage) runs on the libGDX `Timer` thread, guarded by hand-rolled `synchronized`/`shutdownBlocking`, racing the main-thread `update()`. `GameWorld.setMap` even logs a warning when mutated off the main thread. `AppDispatchers` exists but is barely used.

## Decision

One **simulation coroutine** per `SessionScope` owns all mutable world + ECS state — a **single writer**. The only way to mutate state is the **command bus** (`CommandContext.submit`). Heavy pure work (worldgen, lighting BFS, meshing, save/snapshot serialization) runs on `Dispatchers.Default`/`io` under structured concurrency and returns results as commands applied on the sim thread.

## Consequences

- Eliminates the `Timer`+lock races and the "off main thread" warning.
- The command bus is also the seam for networking (ADR 0005): local input vs replicated commands.
- All gameplay writes funnel through one place — easy to log, test, and replicate.
