# Architecture Decision Records — CaveDroid 2.0

These ADRs capture the decisions behind the ground-up **2.0** refactor (game-agnostic engine, Ashley ECS, ticks, server-authoritative networking). Big-bang on branch `refactor/2.0`; the stable line is kept for hotfixes.

Tracking: GitHub milestone **CaveDroid 2.0**, apex issue **#143**.

| ADR | Decision |
|---|---|
| [0001](0001-game-agnostic-engine.md) | Game-agnostic engine: content registry + capabilities (no `Block` sealed class in the engine) |
| [0002](0002-ashley-ecs-big-bang.md) | Ashley ECS; big-bang refactor shipped as 2.0 on a branch |
| [0003](0003-single-writer-command-bus.md) | Single-writer simulation + command bus on coroutines |
| [0004](0004-minecraft-ticks.md) | Minecraft-style ticks: 20 TPS authoritative + Box2D subticks + render interpolation |
| [0005](0005-server-authoritative-networking.md) | Networking: server-authoritative + client prediction; single-player = loopback |
| [0006](0006-context-parameters.md) | Kotlin context parameters for ambient contexts (replaces world adapters) |
| [0007](0007-interaction-context.md) | `InteractionContext`: single source of truth for input/render mode |
| [0008](0008-build-catalog-jdk.md) | Build: version catalog + build-logic; JDK 25 toolchain / Java 8 target |
| [0009](0009-gdx-ai.md) | AI via gdx-ai (behavior trees + FSM + steering); pathfinding deferred |
| [0010](0010-engine-capabilities-saving-menu-mvvm.md) | Saving and menu MVVM are engine capabilities (menu MVVM redesigned) |
| [0011](0011-scripting-and-command-system.md) | Scripting (Lua/LuaJ) + CLI + command executor as an engine capability; use-actions become scripts |

Status legend: **Accepted** — agreed; will be implemented as part of the 2.0 epics.
