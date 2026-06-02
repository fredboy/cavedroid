# 0010 — Saving and menu MVVM are engine capabilities

- Status: Accepted
- Epics: E7, E10

## Context

The engine must be a genuine reusable engine (ADR 0001), so saving and menu UI structure belong to the engine, not only to game code. The current menu MVVM (`core:common:mvvm` + `core:gdx/.../menu/v2/navigation`) is mediocre.

## Decision

**Saving/persistence is an engine capability (E7):** the engine provides the save/load framework — serialize world + ECS components + game state, snapshots — and the game only declares *what* is serialized. This shares the serialization layer with networking snapshots (ADR 0005). 1.x save compatibility is likely dropped for 2.0.

**Menu MVVM is an engine capability (E10):** a proper game-agnostic MVVM/MVI + navigation framework over scene2d (reactive view models, state, back-stack). The current implementation is **redesigned, not ported**. The screen stack is shared with the in-game `InteractionContext` (ADR 0007). Platform/capability gates stay in the view model, not the view.

## Consequences

- Save/replication serialization is one framework, used by both persistence and networking.
- Menus get a clean reactive framework; a second game inherits both capabilities.
- Requires designing a serialization contract for components up front.
