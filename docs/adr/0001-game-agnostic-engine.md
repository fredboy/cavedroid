# 0001 — Game-agnostic engine: content registry + capabilities

- Status: Accepted
- Epics: E1, E3

## Context

In 1.x the "domain" model is already game-specific: `Block` is a `sealed class` with Minecraft subtypes (`Furnace`, `Chest`, `Water`, `Lava`, `Ladder`, `Web`, `Fire`, `Slab`) living in `core:domain:items`. Any new block edits a sealed hierarchy in the core, and the engine layers are fused with the game.

## Decision

Split into three tiers: `engine:*` (fully game-agnostic) / `cavedroid:*` (content + rules + UI + worldgen) / platform launchers.

The engine never knows about concrete blocks. `Block` stops being a sealed class; it becomes:

- a **content registry** of `TileDefinition`s, registered by `cavedroid:content`;
- **capability components** the engine understands generically: `ContainerCapability`, `FluidCapability { state }`, `ClimbableCapability { speedFactor }`, `LightEmitterCapability`, …

The engine provides a generic `TileWorld` (layered grid + streaming). Game-specific rules (fluid flow, fire spread, crafting) live in `cavedroid:systems`/`cavedroid:content`.

## Consequences

- New content is data + capabilities, not edits to engine code.
- A second game on the engine becomes possible (the chosen "full engine" depth).
- More upfront abstraction work; capability surface must be designed deliberately.
