---
name: cavedroid-unit-tests
description: Add or fix CaveDroid Kotlin JVM unit tests using the project's test stack and libGDX/native constraints. Excludes instrumentation and manual playtests.
---

# CaveDroid JVM unit tests

Use JUnit Jupiter, mockk, and `kotlinx-coroutines-test` as needed. Versions come
from `gradle/libs.versions.toml`. Follow [AGENTS.md](../../../AGENTS.md) for the
JDK and module boundaries.

## Setup

Put tests in the owning module's `src/test/kotlin/`, matching the production
package. Use descriptive backtick method names like the existing tests.

For a Kotlin JVM module without test wiring, follow `core/common/build.gradle.kts`:

```kotlin
dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
}
```

Add `libs.mockk` and `libs.kotlinx.coroutines.test` only when needed. Android has
its own unit-test task configuration; do not copy JVM task wiring blindly.

## libGDX boundaries

JVM tests have no initialized libGDX environment. Avoid constructing objects
that allocate GL resources or native Box2D handles. Pure values such as `Vector2`
and `Rectangle` are safe. Mock or fake platform boundaries; if setup overwhelms
the behavior being tested, extract pure logic into the owning feature module.

Use `runTest` for coroutine tests. Existing examples are under
`core/common/mvvm/src/test/kotlin/`: `ViewModelTest` covers coroutines and
`NavRootStageTest` demonstrates a fake that avoids constructing a GL-backed stage.

## Verification

Run `./gradlew :<module>:test` for the affected JVM module, or `./gradlew testCore`
for all core tests. Confirm tests executed in the task output or the module's
`build/reports/tests/test/` report. Report any checks that could not run.
