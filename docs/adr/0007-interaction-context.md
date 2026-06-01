# 0007 — InteractionContext: single source of truth for input/render mode

- Status: Accepted
- Epics: E6

## Context

Input and rendering are two independent dispatch loops over DI-collected `Set`s. Each input handler's `checkConditions(action)` probes global state (`GameWindowsManager.currentWindow`, `MobController.player.isDead`); each renderer reads game state directly and is ordered by a magic `renderLayer: Int`. "Is the chest open?" is decided twice, in two subsystems — the coupling the maintainer flagged.

## Decision

Introduce one `InteractionContext` state machine (`Gameplay`, `Window(...)`, `Paused`, `Dead`) owning "which mode we are in".

- Input: raw libGDX events → `InputIntent` (the only place that knows keys/touch/bindings) → a router dispatches by the **current** `InteractionContext`. Per-handler `checkConditions` is removed.
- Rendering: an **explicit ordered pipeline** (not a `Set` sorted by `renderLayer`) reads a view snapshot; the same `InteractionContext` selects the active HUD/window layer.

`engine:scene` owns the `InteractionContext` and screen stack, shared with the menu navigation (ADR 0010).

## Consequences

- Mode lives in one place; input routing and rendering both read it instead of re-deriving it.
- Handlers become pure: "given this intent in this context, emit these commands".
