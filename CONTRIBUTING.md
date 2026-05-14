# Contributing to CaveDroid

Thanks for your interest in contributing! This document covers how to set up your environment, where to send changes, and the project's coding conventions.

For building and running the game, see [README.md](README.md). For the codebase walkthrough (modules, DI, per-platform notes), see [CLAUDE.md](CLAUDE.md).

## Setup

You need:

- **JDK 17** (Temurin, Zulu, Adoptium — must be exactly 17; the build pins JVM target 17 everywhere)
- Git
- (Optional) Android SDK if you want to build the Android target locally

Verify your environment works:

```bash
./gradlew desktop:run
```

## Workflow

1. **Fork** the repository to your GitHub account.

2. **Branch off `develop`** (not `master`). Pick a name that matches one of these patterns:

   | Pattern | Use when |
   |---|---|
   | `issue/<number>` | The change addresses an existing GitHub issue (e.g. `issue/42`). |
   | `fix/<short-slug>` | Bug fix without a dedicated issue (e.g. `fix/lighting-flicker`). |
   | `feature/<short-slug>` | New feature without a dedicated issue (e.g. `feature/wolf-mob`). |

   ```bash
   git checkout develop
   git pull
   git checkout -b issue/42
   ```

3. **Commit.** If the branch is `issue/NN`, prefix every commit subject with `#NN: ` — e.g. `#42: Add inventory drag-and-drop`. This keeps GitHub cross-linking intact and is enforced by CI. Other branches have no prefix requirement.

4. **Open a PR targeting `develop`.** Reference the issue in the description (`Closes #42`).

CI runs ktlint, JUnit tests, an Android debug build, and lints your branch name + commit messages. All must pass before merge.

## Branching context

```
master ────●─────●─────●────▶   (latest stable — never target this)
            \   / \   /
release/*    ◯◯   ◯◯           (release branches — maintainer-only)
            /     \
develop ●──●───●───●───●────▶   (target your PRs here)
            \
             issue/42           (your branch)
```

Always target `develop`. Release branches and `master` are handled by the maintainer.

## Code style

- **Kotlin only** — no new Java. JVM target 17 throughout.
  - Exception: vendored emulated classes under `html/src/main/java/emu/**` and `html/src/main/java/emulate/**`.
- **Ktlint** (1.6.0) gates CI:

  ```bash
  ./gradlew ktlintCheck buildSrc:ktlintCheck   # check
  ./gradlew ktlintFormat                       # auto-format
  ```

- `.editorconfig` disables a few rules (multiline expression wrapping, string template indent, comment wrapping, empty first line in class body, property/backing-property naming, function expression body) — don't reintroduce them.

## Testing

Tests use JUnit Jupiter (5.x), mockk, and `kotlinx-coroutines-test`.

```bash
./gradlew test                  # all modules
./gradlew :core:common:test     # a single module
```

Don't instantiate libGDX classes that need a GL context (`Stage`, `RayHandler`, `PointLight`, etc.) in unit tests — mock them or extract logic into a pure helper in `core:common`. See the "Tests" section in [CLAUDE.md](CLAUDE.md) for the per-module `build.gradle.kts` block.

## Pull requests

- Target `develop`.
- One issue per PR where possible; reference the issue (`Closes #42`).
- CI must pass before merge.
- Default to merge commits (`--no-ff`); squash only when the branch history is noisy.

## Questions

Open an issue or comment on an existing one.
