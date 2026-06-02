# 0008 — Build: version catalog + build-logic; JDK 25 toolchain / Java 8 target

- Status: Accepted
- Epics: E0

## Context

1.x dependency management is hand-rolled in `buildSrc` (`Dependencies.kt`, `Versions.kt`, `DependencyHandlerExtentions.kt` with `useCommonLibs()`/`useDagger()`/`useDomainModules()`). About to add ~20 new modules — they should be authored in the target style from day one. All dependencies (and the JDK) are being bumped.

## Decision

- **`gradle/libs.versions.toml`** version catalog for versions/libraries/bundles/plugins (replaces `Dependencies.kt`/`Versions.kt`).
- **`build-logic` convention plugins** for behavior the catalog can't express: `cavedroid.kotlin-library` (JVM target, ktlint, **tests wired by default**), `cavedroid.dagger` (KSP + Dagger + automultibind), `cavedroid.libgdx`, etc.
- **Type-safe project accessors** (`projects.*`) replace `useDomainModules()`-style helpers.
- **Bump all dependencies.**
- **Toolchain = JDK 25 (LTS)**; **target = Java 8** byte+API: `jvmTarget = "1.8"` + `-Xjdk-release=1.8` (Kotlin) + `--release 8` (Java emu). Shared/shipped code is API-capped at Java 8 (plus Android desugaring); a platform module may target higher only if its runtime guarantees it.

Java 6 is impossible (Kotlin dropped jvmTarget 1.6 in Kotlin 1.6). The Java 8 floor keeps RoboVM/iOS likely free; **TeaVM is the gating target**.

## Consequences

- Module build files read like manifests on standard rails.
- One place to bump versions; new modules start correct.
- Must guard against accidental post-8 API usage in shared code (the `-Xjdk-release`/`--release` flags enforce it).
