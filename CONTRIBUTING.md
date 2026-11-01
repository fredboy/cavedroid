# Contributing to CaveDroid

See [README.md](README.md) for setup and build commands, and [AGENTS.md](AGENTS.md)
for the code map and implementation constraints.

## Workflow

- Branch from `develop` and target it with your PR. Use `issue/<number>`,
  `fix/<short-slug>`, or `feature/<short-slug>` for branch names.
- Start PR titles with a capital letter. For `issue/NN`, include the matching
  closing reference, for example `Closes #42: Fix inventory dragging`.
- Keep changes focused. Feature PRs are squash-merged; commit subjects have no
  required format.
- Check the workflows in [.github/workflows/](.github/workflows/) for current CI
  checks and branch/title validation. Release branches are maintained separately.

## Code and tests

Write Kotlin and follow `.editorconfig`. Existing Java emulation sources in
`html/` are the exception. Keep dependency versions in
[gradle/libs.versions.toml](gradle/libs.versions.toml).

Tests use JUnit Jupiter, with mockk and `kotlinx-coroutines-test` as needed.
Follow an existing tested module's setup. Avoid GL and native physics in JVM
unit tests; mock those boundaries or extract pure logic into the owning module.

Run checks appropriate to your change with JDK 25:

```bash
./gradlew testCore
./gradlew ktlintCheck buildSrc:ktlintCheck
```

For a single module, use its test task, such as `./gradlew :core:common:test`.
For gameplay, rendering, or input changes, also check the affected behavior in-game
when possible. Report what you verified and any limitations.

For custom textures and gameplay data, see [ASSET_PACKS.md](ASSET_PACKS.md).
