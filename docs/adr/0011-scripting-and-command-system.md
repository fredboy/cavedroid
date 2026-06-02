# 0011 — Scripting & command system (engine capability)

- Status: Accepted
- Epics: E11 (depends on E2; reshapes E4; CLI UI via E6)

## Context

Block/item "use" actions are hardcoded in Kotlin (`UseWaterBucketAction`, …). We want behavior to be **data/script-driven**, plus an in-game **CLI** and a **command executor** (Minecraft-style `/give`, `/tp`). This must be an **engine** capability (game-agnostic), like saving (ADR 0010) — the game supplies scripts and commands, the engine supplies the runtime.

Three distinct layers, often conflated:
1. **Internal command bus** (ADR 0003) — mutation primitives (`SetBlock`, `SpawnEntity`, `PlaySound`). Single writer. Not user-facing.
2. **CLI / command executor** — text (`/give`) → parser → command registry → handlers that emit bus commands. Generalizes the existing `CommandExecutor`.
3. **Behavior scripts** — use-block/use-item behaviors written in a scripting language, registered per content definition (ADR 0001).

Key insight: both CLI handlers and behavior scripts are just **producers of bus commands**; their host-API bindings are the **contexts** (`WorldContext`/`CommandContext`/`ClockContext`/`EcsContext`, ADR 0006). Scripting sits on top of the command bus.

## Decision

Scripting + CLI + command executor are **engine capabilities**. The engine provides the runtime, the sandboxed host-API (= contexts), the CLI console (a mode in the `InteractionContext`, ADR 0007), and the command parser/registry. `cavedroid` provides the scripts (use behaviors) and the concrete command set.

**Runtime: Lua via LuaJ** (pure-Java interpreter). Chosen for expressiveness and a familiar modding language.

**Boundary — event-driven is scripted, hot paths stay native:** use-actions, CLI commands, and (optionally) crafting are Lua. Hot per-tick systems (fluids, physics, lighting, random-tick) remain native Kotlin for performance and determinism. This reshapes E4: Kotlin `*Action` classes become Lua scripts; per-tick systems stay Kotlin.

**Constraints (hard):**
- Must run on **TeaVM/web** and **RoboVM/iOS** — no runtime codegen/compiler. LuaJ must be verified on TeaVM (gating spike, like S0).
- **Determinism** for server-authoritative reconcile (ADR 0005): scripts run in the authoritative tick on the server; route all time/rng through `ClockContext` (seeded), eliminate nondeterministic Lua behavior (table iteration order, `os`/`io`).
- **Sandbox**: no IO/reflection; bounded execution (instruction/step limit) so a script can't hang the tick.

## Consequences

- Use behaviors become content (scripts), not engine/game Kotlin — strengthens the game-agnostic engine (ADR 0001).
- One runtime serves both CLI and behaviors; one host-API (contexts).
- Risks to retire via the spike: LuaJ-on-TeaVM, determinism hardening, sandbox/limits, interpreter perf on mobile/web.
- Modding becomes feasible (scripts + content registry).
