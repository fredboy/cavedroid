# 0005 — Networking: server-authoritative + client prediction

- Status: Accepted
- Epics: E8

## Context

2.0 must be ready for multiplayer. Box2D and randomness make strict lockstep determinism impractical.

## Decision

**Server-authoritative** simulation behind a command interface. The server runs the authoritative 20 TPS tick (with subticks, ADR 0004) and broadcasts snapshots + events.

- The client **predicts only the local player**, at the **same 20 TPS** as the server (identical `dt` and subtick scheme), and reconciles by replaying unacknowledged inputs when the authoritative snapshot arrives.
- 60 Hz on the client is **render interpolation + reconcile smoothing only**, never a second physics rate.
- Remote entities (mobs, other players) are **interpolated** from snapshots, not predicted.
- **Single-player = a co-located server over a loopback transport**, so SP and MP share one code path.

## Consequences

- Robust to Box2D non-determinism: only the local player body is predicted; corrections are smoothed.
- Requires serializable components/commands/snapshots (shares the serialization layer with saving, ADR 0010).
- Rendering/input are strictly client-side; simulation strictly server-side, even in SP.
