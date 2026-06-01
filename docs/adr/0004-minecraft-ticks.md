# 0004 — Minecraft-style ticks: 20 TPS + Box2D subticks + interpolation

- Status: Accepted
- Epics: E2

## Context

We want proper tick semantics like Minecraft. Box2D wants ~60 Hz for solver stability, while game logic wants a fixed authoritative rate that can be replicated.

## Decision

- Authoritative unit = **game tick at 20 TPS** (50 ms). `gameTick` is the replicated counter.
- Inside each tick, Box2D integrates in **fixed subticks** (e.g. 3×1/60) for stability.
- **Rendering is decoupled** and runs at display FPS, interpolating between the last two ticks via `partialTick` (read from a snapshot). No separate client-side physics rate.
- A `TickScheduler` provides **scheduled tile ticks** (delay + priority) and **random ticks** (`randomTickSpeed`). The 1.x periodic `*ControllerTask`s become ordered tick phases / scheduled / random ticks.
- Determinism via a seeded `RandomSource` (see ADR 0006 `ClockContext`).

Rejected: running client physics at a free 60 Hz decoupled from the tick — different `dt` from the server diverges and fights reconciliation.

## Consequences

- Keeps Box2D and the current movement feel; physics stays non-deterministic but that is tolerated by server-authority (ADR 0005).
- Tick behavior (fluids, fire, growth) becomes golden-testable.
