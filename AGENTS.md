# CaveDroid agent guide

CaveDroid is a Kotlin/libGDX 2D sandbox game for desktop, Android, iOS,
and web (TeaVM). Use the Gradle wrapper from the repository root with JDK 17.

## Code map

- `core/domain/`: models, repository interfaces, and use cases.
- `core/data/`: repository implementations, including asset and save loading.
- `core/entity/`: mobs, drops, containers, and projectiles.
- `core/game/`: world simulation, controllers, and inventory/crafting windows.
- `core/gameplay/`: controls, physics, rendering, and lighting.
- `core/gdx/`: application setup, screens, menus, and DI integration.
- `core/common/`: shared contracts and utilities.
- `android/`, `desktop/`, `ios/`, `html/`: platform launchers and integrations.

Use `settings.gradle.kts` and module build files for the current dependency
graph. Follow existing module boundaries; place helpers with the feature that
owns them unless they are shared by multiple features.

## Conventions and traps

- The Y axis points down.
- Write Kotlin and follow `.editorconfig`. Existing Java emulation sources in
  `html/src/main/java/emu/` and `html/src/main/java/emulate/` are the exception.
- Use dependency definitions and helpers in `buildSrc/`; centralize versions there.
- DI uses Dagger, KSP, and automultibind. Follow existing annotations when adding
  handlers, actions, or renderers; their binding modules are generated. For
  explicit bindings, check the owning component and module wiring in `core/gdx/`.
- Match dependency lifetimes to their owning scope: application (`@Singleton`),
  menu (`@MenuScope`), or game session (`@GameScope`).
- Keep platform/capability checks for menu UI in view models.
- Use Kermit with `Logger.withTag(...)` for logging.
- Edit shared assets in `assets/`. `desktop/src/main/resources` and
  `android/src/main/assets` are symlinks there; preserve them.
- Do not commit generated DI code, `notices.txt`, or `attribution_index.txt`.
- Bump versions with `scripts/up-version.sh <version>` so both version
  definitions stay synchronized.

## Verification

Choose checks appropriate to the affected modules and behavior:

| Purpose | Command |
| --- | --- |
| Run desktop | `./gradlew desktop:run` |
| Run with touch controls | `./gradlew desktop:runTouch` |
| Build unsigned desktop JAR | `./gradlew desktop:dist` |
| Build Android FOSS debug APK | `./gradlew :android:assembleFossDebug` |
| Compile web target | `./gradlew html:buildJs` |
| Run module tests (substitute module path) | `./gradlew :core:common:test` |
| Run all core tests | `./gradlew testCore` |
| Check Kotlin style | `./gradlew ktlintCheck buildSrc:ktlintCheck` |

Tests use JUnit Jupiter, with mockk and kotlinx-coroutines-test where needed.
Modules with tests must enable `tasks.test { useJUnitPlatform() }` and declare
test dependencies through the definitions in `buildSrc/`. Use an existing
tested module such as `core/common` as a setup example.

Avoid constructing objects that need GL or native physics in JVM tests.
Mock those boundaries or extract pure logic into the appropriate module.
Verify rendering and input changes in-game when desktop interaction is
available. Report what was actually checked and any verification limitations.

## Task-specific guidance

- Setup and contribution workflow: [CONTRIBUTING.md](CONTRIBUTING.md).
- Release procedure: [RELEASING.md](RELEASING.md).
- Locale additions, legacy Android builds, and unit-test recipes:
  relevant `SKILL.md` files under `.agents/skills/`.

Keep this guide focused on durable constraints and hard-to-discover traps.
Detailed feature behavior belongs with its implementation or focused docs.
