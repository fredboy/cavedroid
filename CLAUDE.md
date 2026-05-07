# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

CaveDroid — a 2D Minecraft-inspired game built on **libGDX** in **Kotlin** (JVM 17, Kotlin 2.2.10), shipping for Android, Desktop (Linux/Windows/macOS via the `construo` plugin), and iOS (via RoboVM). Single-player, horizontally looped world.

## Common commands

All Gradle tasks run via `./gradlew` (use `gradlew.bat` on Windows — note that the asset symlinks break the Windows build out of the box).

```bash
# Run desktop game (depends on assemble; uses assets dir as working dir)
./gradlew desktop:run                  # mouse/keyboard
./gradlew desktop:runTouch             # forces touch-screen mode (--touch --debug)

# Build Android debug APK
./gradlew android:assembleDebug

# Build a fat desktop JAR (no signing/proguard)
./gradlew desktop:dist

# Cross-compiled desktop bundles (uses construo, downloads target JDKs)
./gradlew desktop:packageLinuxX64
./gradlew desktop:packageWinX64
./gradlew desktop:packageMacM1

# Signed/proguarded desktop release JAR (requires keystore.properties)
./gradlew desktop:generateSignedJar

# Lint
./gradlew ktlintCheck                  # check
./gradlew ktlintFormat                 # auto-format
./gradlew buildSrc:ktlintCheck         # CI also runs this for buildSrc

# Tests (JUnit Jupiter)
./gradlew test                         # run tests across all modules
./gradlew :core:common:test            # run a single module's tests

# Generate THIRD-PARTY notices (auto-runs as part of processResources/preBuild)
./gradlew generateLicenseReport
```

### Tests

The repo is incrementally growing a unit test suite. Tests live alongside the
production code under `src/test/kotlin/...` in modules that have opted in.
Conventions, derived from the existing tests in `core/common/mvvm` and
`core/common`:

- **JUnit Jupiter** (5.x) — `useJUnitPlatform()` is required in the module's
  `tasks.test {}` block. Add it explicitly when introducing tests to a new
  module — it is not enabled by default by the Kotlin JVM plugin.
- **mockk** for test doubles. Avoid instantiating libGDX classes that need a GL
  context (`Stage`, `RayHandler`, `PointLight`, etc.) in unit tests — either
  mock them or extract the logic under test into a pure helper.
- **kotlinx-coroutines-test** for `runTest` when the unit under test launches
  coroutines (see `core/common/mvvm`).
- Test dependencies are referenced through `Dependencies.Test.*` and
  `Dependencies.Kotlin.coroutinesTest`. Add the standard block to the module's
  `build.gradle.kts`:

  ```kotlin
  testImplementation(Dependencies.Test.junitJupiter)
  testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
  testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
  testImplementation(Dependencies.Test.mockk)              // optional
  testImplementation(Dependencies.Kotlin.coroutinesTest)    // optional

  tasks.test { useJUnitPlatform() }
  ```

When the SUT is heavy (Box2D bodies, RayHandler, Stage), prefer extracting the
testable logic into a pure helper in `core:common` and unit-testing the helper.
Game-loop / rendering behavior is still verified manually via
`./gradlew desktop:run`.

### Release flow

`./make-release.sh <versionName>` is the canonical release script: it requires a clean tree, runs `up-version.sh` (which bumps `versionName` and increments `versionCode`), runs `ktlintCheck` + Android release + Linux/Win desktop packages, copies artifacts into `release-<version>/`, generates a changelog, and creates the commit + tag. Don't hand-edit version numbers — `up-version.sh` keeps two files in sync (see "Versioning" below).

### Signing

For Android release builds and the desktop `generateSignedJar`/`packageLinuxX64`/`packageWinX64` tasks, create `keystore.properties` at repo root with `releaseKeystorePath`, `releaseKeystorePassword`, `releaseKeyAlias`, `releaseKeyPassword`. Without this file, debug builds still work; CI mocks it (`echo "releaseKeystorePath=mock" > keystore.properties`).

## Architecture

### Multi-module Gradle layout

Three platform launcher modules (`android`, `desktop`, `ios`) thinly wrap a shared `core` module graph. The launcher's only job is to build a `CaveDroidApplication` (in `core:gdx`) and hand it a platform-specific `PreferencesStore`.

The `core` graph follows a clean-architecture-ish split. **Module dependencies flow inward:** outer layers depend on inner ones, never the reverse.

- **`core:common`** — shared API contracts (`ApplicationController`, `PreferencesStore`, `SoundPlayer`), constants, scope annotations (`@GameScope`, `@MenuScope`), Kotlin/Gdx utilities. No libGDX-specific game logic.
- **`core:domain:*`** — repository interfaces, use cases, domain models (assets, configuration, items, world, save). Pure Kotlin.
- **`core:data:*`** — repository implementations and Dagger modules that bind them (assets, configuration, items, save). Save uses `kotlinx.serialization` (json + protobuf).
- **`core:entity:*`** — game entity types: `container`, `drop`, `mob`, `projectile`.
- **`core:game:*`** — game-scoped logic: `controller/{drop,container,mob,projectile}`, `world`, `window` (UI windows like inventory, crafting). Lives behind `@GameScope`.
- **`core:gameplay:*`** — cross-cutting systems: `controls`, `physics` (Box2D + Box2DLights), `rendering`.
- **`core:gdx`** — top-level integration: the `CaveDroidApplication : Game` class, `MenuScreen`/`GameScreen`/`PauseMenuScreen`, the menu's MVVM-ish navigation framework (`menu/v2/navigation`), and the **two Dagger components** (`ApplicationComponent`, `GameComponent`).

The `buildSrc/` module centralizes versions and exposes ergonomic dependency helpers: each module's `build.gradle.kts` reads almost like a manifest (`useCommonLibs()`, `useDagger()`, `useDomainModules()`, `useGameModules()`, `useLibgdx()`, `useLibKtx()`, `useAutomultibind()`, `useKotlinxSerializationJson()`, etc., defined in `buildSrc/src/main/kotlin/DependencyHandlerExtentions.kt`). When adding dependencies, prefer extending these helpers over inlining versions.

### Dependency injection (Dagger + KSP + automultibind)

There are **two scopes / components**:

1. **`ApplicationComponent`** (`@Singleton`) — built in `CaveDroidApplication.create()` from an `ApplicationContext` (debug flag, touch flag, save dir, viewport, locale, etc.). Owns assets, item/mob repositories, save data, screens.
2. **`GameComponent`** (`@GameScope`) — `dependencies = [ApplicationComponent::class]`, built when a game session starts; carries a `GameContext`, the Box2D world, controllers, and the renderer.

Multibindings are produced by **`ru.fredboy:automultibind`** (an in-house KSP processor). Annotated implementations are auto-collected into generated Dagger modules under the package `ru.fredboy.cavedroid.generated.module` (e.g. `KeyboardInputHandlersModule`, `MouseInputHandlersModule`, `WorldRenderModule`, `HudRenderModule`, `PlaceBlockActionsModule`, `UseBlockActionsModule`, `UseItemActionsModule`, `UseMobActionsModule`, `UpdateBlockActionsModule`, `DropContactHandlerModule`, `MobContactHandlerModule`, `ProjectileContactHandlerModule`). These modules don't exist on disk — they're regenerated on every KSP run. To add a new handler/action, annotate the implementation and let KSP wire it in; don't edit a generated module by hand.

When changing DI wiring, also update the matching `Component` in `core:gdx/.../di/` and any module list in `GameModule`/`ApplicationModule` if you've added a non-multibound module.

### Assets and resources

`assets/` at the repo root is the single source of truth. Both `desktop/src/main/resources` and `android/src/main/assets` are **symlinks** to it (which is why Windows builds need extra setup). The iOS module reads `assets/` directly. Each platform's `build.gradle.kts` adds two synthesized resources at build time:

- `notices.txt` — generated from the license report plugin (`copyLicenseReport` → `build/generated/extraRes/notices.txt`).
- `attribution_index.txt` — built by the `generateAttributionIndex` task by walking the assets tree for `attribution.txt` files.

Both wire into `processResources` / `preBuild` automatically — don't commit them.

### Versioning

`buildSrc/src/main/kotlin/ApplicationInfo.kt` (`versionName`, `versionCode`) and `core/common/.../CaveDroidConstants.kt` (`VERSION`) must stay in lockstep. `up-version.sh <new-version>` is the only sanctioned way to bump them.

### Logging

`co.touchlab:kermit` everywhere. The launcher chooses severity from CLI flags (`--verbose` → Verbose, `--debug` → Debug, otherwise Info). Use `Logger.withTag("…")` per class rather than the global logger.

## Style

- **Kotlin only** (no new Java code). JVM target 17 across all modules.
- **Ktlint** (1.6.0, IntelliJ code style) gates CI. The `.editorconfig` disables a handful of rules (multiline expression wrapping, string template indent, comment wrapping, empty first line in class body, property/backing-property naming, function expression body) — don't reintroduce those.
- Generated code under `**/generated/**` is excluded from formatting.
