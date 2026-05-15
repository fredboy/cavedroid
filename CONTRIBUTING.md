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

3. **Commit.** Commit subjects have no required format — feature PRs are **squash-merged**, so the PR title becomes the single commit landing on `develop`.

4. **Open a PR targeting `develop`.** Title rules:
   - **All PRs:** the title must start with a capital letter.
   - **`issue/NN` branches** additionally: the title must start with a GitHub issue-closing keyword and the matching issue number, e.g.
     - `Closes #42: Add inventory drag-and-drop`
     - `Fixes #99 - Crash on world deletion`
     - `Resolves #100 Add new feature`

     Allowed keywords: `Close` / `Closes` / `Closed`, `Fix` / `Fixes` / `Fixed`, `Resolve` / `Resolves` / `Resolved` (the first letter must be uppercase; the rest of the keyword is case-insensitive). CI enforces this and that the issue number matches the branch.
   - **`fix/<slug>` / `feature/<slug>`:** any title is fine as long as it starts with a capital letter. If the change happens to close an issue, include a closing keyword in the title or description so GitHub auto-closes the issue on merge.

CI runs ktlint, JUnit tests, an Android debug build, and lints your branch name + PR title. All must pass before merge.

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
- One issue per PR where possible; the title carries the issue reference (see Workflow step 4).
- CI must pass before merge.
- Feature PRs are **squash-merged** — the PR title becomes the single commit on `develop`. (Release-flow PRs use merge commits, but those are maintainer-only.)

## Questions

Open an issue or comment on an existing one.
