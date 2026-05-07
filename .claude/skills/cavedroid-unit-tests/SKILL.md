---
name: cavedroid-unit-tests
description: Use when the user asks to write, add, or fix unit tests for CaveDroid Kotlin code (any module under core/, android/, desktop/, ios/). Encodes the project's test stack (JUnit Jupiter + mockk + kotlinx-coroutines-test), the libGDX/Box2D/GL constraints that make some classes untestable in JVM tests, the pure-helper-extraction pattern used when the SUT touches GL/native code, and the per-module opt-in build.gradle.kts wiring.
---

# CaveDroid unit tests

## When this applies

User requests anything in the shape of "add tests", "write a test for X", "test this", "unit test the Y manager", on Kotlin code in this repo. Skip for: integration/manual playtest requests, asset/build-system questions, anything that explicitly says "instrumentation test" (the project has none).

## Stack

- **JUnit Jupiter 5** — `org.junit.jupiter.api.Test`, `Assertions.*`. `useJUnitPlatform()` is required in the module's `tasks.test {}` (the Kotlin JVM plugin does not enable it by default — forgetting this means tests silently don't run).
- **mockk** for test doubles. Don't bring in Mockito or PowerMock.
- **kotlinx-coroutines-test** (`runTest`, `TestScope`) when the SUT launches coroutines or uses `viewModelScope`.
- Test deps come from `Dependencies.Test.*` (`buildSrc/src/main/kotlin/Dependencies.kt`) — never inline a Maven coordinate string.

## Layout

- Tests live alongside production code at `<module>/src/test/kotlin/<same package as SUT>/<Name>Test.kt`.
- One test class per SUT. Class name = `<SUT>Test`.
- Test method names use backtick-quoted English: `` `dispose cancels viewModelScope` `` — match the style in `core/common/mvvm/src/test/kotlin/.../*.kt`.

## Reference tests (read before writing new ones)

- `core/common/mvvm/src/test/kotlin/.../ViewModelTest.kt` — coroutines + `runTest` + state observation.
- `core/common/mvvm/src/test/kotlin/.../NavBackStackTest.kt` — mockk `relaxed = true` host, `verify`/`verifyOrder`/`confirmVerified`.
- `core/common/mvvm/src/test/kotlin/.../NavRootStageTest.kt` — illustrates the hand-rolled fake pattern when a real libGDX `Stage` would need a GL context.
- `core/common/src/test/kotlin/.../utils/WorldEdgeMirrorTest.kt` — pure JVM logic test for an extracted helper; the textbook example for the extraction pattern below.

## The libGDX / Box2D / GL constraint (read this before mocking)

JVM unit tests have **no GL context**. Any libGDX type whose constructor or methods touch `Gdx.gl`, GL shaders, framebuffers, or native JNI handles will NPE or crash at runtime. Concretely, do **not** instantiate these in unit tests:

- `box2dLight.RayHandler`, `box2dLight.PointLight`, `box2dLight.DirectionalLight` — shader/framebuffer in constructor.
- `com.badlogic.gdx.scenes.scene2d.Stage` — needs a viewport with a GL context.
- `com.badlogic.gdx.physics.box2d.ChainShape`, `PolygonShape`, `World`, `Body`, `Fixture` — JNI handles; require Box2D natives to be loaded (`GdxNativesLoader.load() + Box2D.init()`). Possible but heavy — avoid unless the test really needs it.
- `com.badlogic.gdx.graphics.g2d.SpriteBatch`, `Texture`, `BitmapFont` — GL.
- Anything reading `Gdx.app`, `Gdx.files`, `Gdx.input` without a `HeadlessApplication`.

Plain data classes (`Vector2`, `Rectangle`, `Color`) are fine — pure Java/Kotlin.

## The pattern: extract pure helpers, test the helpers

When the SUT directly couples to one of the un-testable types above, **don't** try to mock around it. Extract the testable logic into a pure Kotlin helper (typically in `core:common/.../utils/`) and test that. The manager keeps using the helper; the test exercises only the helper.

Worked example (already in the repo):
- SUT: `GameWorldLightManager` constructs `RayHandler` + `PointLight` → cannot be instantiated in tests.
- Logic worth testing: which chunks need edge-mirroring, on which sides, with what band clamping.
- Extraction: `core/common/.../utils/WorldEdgeMirror.kt` — pure top-level functions `effectiveMirrorBand()`, `mirrorSidesFor()`.
- Test: `core/common/src/test/kotlin/.../utils/WorldEdgeMirrorTest.kt`. 14 cases covering boundaries, clamping, invariants. No libGDX touched.

Rule of thumb: if mocking forces you to stub out half the libGDX surface, stop and extract instead.

## Mocking guidance

- Default to `mockk<T>(relaxed = true)` when you only care about verifying calls or returning canned values.
- Use `verify { ... }`, `verify(exactly = N) { ... }`, `verifyOrder { ... }`, `confirmVerified(mock)` to pin the contract.
- Prefer hand-rolled fakes (see `FakeNavHost` in `NavRootStageTest`) over deep `every { ... } returns ...` chains when the test would otherwise duplicate the SUT's logic.
- For coroutines, `runTest { ... }` from `kotlinx.coroutines.test`. Do not use `runBlocking` in tests.

## Per-module opt-in (when the module has no tests yet)

Add to the module's `build.gradle.kts`:

```kotlin
dependencies {
    // ...existing implementation deps...

    testImplementation(Dependencies.Test.junitJupiter)
    testRuntimeOnly(Dependencies.Test.junitJupiterEngine)
    testRuntimeOnly(Dependencies.Test.junitPlatformLauncher)
    testImplementation(Dependencies.Test.mockk)              // only if needed
    testImplementation(Dependencies.Kotlin.coroutinesTest)   // only if needed
}

tasks.test {
    useJUnitPlatform()
}
```

Then create `src/test/kotlin/<package path>/`.

Don't add test deps to a module that already has them. Check first:

```bash
grep -l "useJUnitPlatform" <module>/build.gradle.kts
```

## Running tests

```bash
./gradlew :core:common:test                  # one module
./gradlew test                               # all modules
./gradlew :core:common:test --rerun-tasks    # force re-run after non-source changes
```

Test reports land at `<module>/build/reports/tests/test/index.html`; XML summaries at `<module>/build/test-results/test/`.

## Style — match the existing tests

- Backtick-quoted descriptive method names. Sentence case. No "should".
- One concept per test. If asserting more than 2-3 things, consider splitting.
- No comments restating what the assertions already say. A short class-level KDoc explaining the *why* of the test class is OK (see `NavRootStageTest`'s docstring on avoiding GL).
- Don't add comments referencing the current change/PR — they rot.
- Don't introduce test utilities prematurely; copy the small fixture pattern from existing tests until duplication actually hurts.

## What not to do

- Don't claim a test exists without running it (`./gradlew :module:test`).
- Don't put tests under `src/main/`; they get compiled into the JAR.
- Don't use Java in tests (project is Kotlin-only — see CLAUDE.md style section).
- Don't add `@DisplayName` annotations; the backticked method names already serve that purpose.
- Don't suppress lint to hush ktlint — fix the style instead. Test sources are linted (`runKtlintCheckOverTestSourceSet`).
- Don't try to test rendering/animation/feature behavior in unit tests — those are verified manually via `./gradlew desktop:run` (per CLAUDE.md).
