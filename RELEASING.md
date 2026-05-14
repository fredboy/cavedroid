# Releasing CaveDroid

Maintainer-only document. Covers the release flow, hotfix flow, signing, secrets, branch protection, and the full CI matrix. Contributors should read [CONTRIBUTING.md](CONTRIBUTING.md) instead.

## Branching model

The project follows **Git Flow**:

- **`master`** — latest stable release commit. Only touched by release / hotfix merges. Tagged with `vA.B.C` for every release.
- **`develop`** — trunk. All new work lands here first. Treat as if every commit could become a release candidate.
- **`release/A.B.C`** — short-lived stabilization branch cut from `develop`. Fixes go in via PR; no direct commits.
- **`hotfix/A.B.C`** — short-lived fix branch cut from `master` when production needs an out-of-cycle patch.

Tags use the form `vA.B.C`. Older tags (`1.0.0`, `1.0.1`, …) pre-date this convention and are kept as-is.

## Release flow

The release logic lives in `scripts/`; GitHub Actions workflows are thin wrappers that call those scripts. **Default path: trigger the workflows from GitHub.** Each step is also runnable locally as a fallback.

### Step 1 — cut the release branch

GitHub: Actions → **Start Release** → Run workflow → `version=A.B.C`. Or:

```bash
gh workflow run start-release.yml -f version=A.B.C
```

Or fully local (must be on `develop`, clean tree):

```bash
./scripts/start-release.sh A.B.C            # creates branch, bumps version, pushes
./scripts/start-release.sh A.B.C --no-push  # same but skip push
```

The script (and workflow) refuse if `release/A.B.C` or `vA.B.C` already exists on origin.

### Step 2 — stabilize

Test the release branch. For each fix, open a PR targeting `release/A.B.C` from a short-lived `fix/<slug>` or `issue/NN` branch. **No direct commits to the release branch** (other than the version-bump commit from Step 1). `release-branch.yml` runs a full multi-platform sanity build on every push/PR.

### Step 3 — finalize

GitHub: Actions → **Finalize Release** → Run workflow → `version=A.B.C`. Or:

```bash
gh workflow run finalize-release.yml -f version=A.B.C
```

Or fully local (must be on `release/A.B.C`, clean tree, `gh` CLI authenticated):

```bash
export GITHUB_TOKEN=$(gh auth token)
./scripts/finalize-release.sh A.B.C                     # waits up to 30 min for master PR to merge
./scripts/finalize-release.sh A.B.C --no-wait           # exits after opening master PR
./scripts/finalize-release.sh A.B.C --timeout 3600      # wait up to 1 hour instead
```

The script:

1. AI-generates en + ru plaintext changelogs via [GitHub Models](https://github.com/marketplace/models).
2. Writes them to `fastlane/metadata/android/{en-US,ru-RU}/changelogs/<versionCode>.txt`.
3. Commits with `[skip ci]` and pushes the release branch.
4. Opens PR `release/A.B.C → master`, enables auto-merge.
5. **Waits for that PR to merge** (you approve it on GitHub).
6. Tags `vA.B.C` on the master merge commit, pushes the tag.
7. Opens PR `release/A.B.C → develop`, enables auto-merge (you approve to sync).
8. Dispatches `release.yml` for the tag.

#### Reviewing the PRs

GitHub forbids an actor from approving their own PR, so the bot cannot self-approve. With "Require pull request reviews" set on `master`/`develop`, **you approve the PRs manually** on GitHub once the workflow opens them. Auto-merge fires automatically as soon as your approval lands and required status checks are green.

If you take longer than the script's wait timeout (default 30 min) to approve, the workflow times out. To recover: approve and merge the master PR by hand, then run the remaining steps (tag, develop PR, dispatch release) — the workflow log prints the exact commands. Or just bump the timeout via `--timeout SECONDS`.

For auto-merge to be available at all, Settings → General → Pull Requests → **Allow auto-merge** must be on.

### Step 4 — artifacts and GitHub Release

After `finalize-release.yml` finishes, `release.yml` runs (dispatched explicitly by the finalize step). It:

1. Calls `scripts/build-release-artifacts.sh A.B.C --skip-legacy` (signed Android foss release + desktop Linux/Win + web).
2. Calls `scripts/gen-release-notes-ai.sh vA.B.C --output release-notes.md` for bilingual markdown release notes.
3. Creates the GitHub Release with all artifacts attached.

To do this locally instead:

```bash
# requires keystore.properties at repo root
./scripts/build-release-artifacts.sh A.B.C
export GITHUB_TOKEN=$(gh auth token)
./scripts/gen-release-notes-ai.sh vA.B.C --output release-A.B.C/RELEASE_NOTES.md
gh release create vA.B.C --notes-file release-A.B.C/RELEASE_NOTES.md release-A.B.C/*
```

Release branches are **not deleted** — they remain for historical reference and as potential hotfix targets.

## Hotfix flow

For production bugs that can't wait for the next normal release:

```bash
git checkout master
git pull
git checkout -b hotfix/A.B.C    # next patch version
./scripts/up-version.sh A.B.C
# commit fixes…
```

Then merge into both `master` and `develop`:

```bash
git checkout master
git merge --no-ff hotfix/A.B.C -m "Merge hotfix/A.B.C into master"
git tag vA.B.C                                       # tag on the master merge commit
git checkout develop
git merge --no-ff hotfix/A.B.C -m "Merge hotfix/A.B.C into develop"
git push origin master develop hotfix/A.B.C vA.B.C
```

Pushing the tag triggers `release.yml` automatically.

Hotfix branches are kept.

> No `start-hotfix.yml` workflow exists yet — hotfixes are rare. If they become frequent, this can be promoted to a workflow_dispatch action symmetric with `start-release.yml`.

## Diagram

```
                                tag vA.B.C
                                    │
master ─────●──────────────●────────●────────────────●──────▶
             \              \      / \              /
              \              \    /   \            /
               \              \  /     \          /
release/A.B.C   \             ◯◯ stabilize         \
                 \           /                      \
                  \         /                        \
develop ───●──────●───●───●─────●────●────●──────────●────────▶
            \              issue/42 merges
             \
              issue/42
```

## Versioning

Three places hold the version and must stay in lockstep:

- `buildSrc/src/main/kotlin/ApplicationInfo.kt` — `versionName` (string), `versionCode` (int).
- `core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt` — `VERSION` (string, mirrors `versionName`).

`scripts/up-version.sh A.B.C` is the only sanctioned way to bump them: it sets `versionName` / `VERSION` to `A.B.C` and increments `versionCode` by one. `start-release.yml` calls this automatically; invoke manually only for hotfix bumps.

## Signing

For local signed builds (signed desktop JAR, packaged desktop bundles, signed Android release), create `keystore.properties` at the repo root:

```properties
releaseKeystorePath=...
releaseKeystorePassword=...
releaseKeyAlias=...
releaseKeyPassword=...
```

CI mocks the file where signing isn't required; `release.yml` constructs it from secrets at build time.

For the `store` Android flavor (Yandex ads), `yandex.properties` is also needed:

```properties
bannerAdUnitId=...
interstitialAdUnitId=...
```

Both files are gitignored.

## Scripts

All release logic lives in `scripts/` so it can run locally and in CI. Each script enforces a repo-root guard.

| Script | Purpose | Local-only requirements |
|---|---|---|
| `up-version.sh A.B.C` | Bump version files in lockstep | Clean tree (called by the others) |
| `require-clean-work-tree.sh` | Helper guard | — |
| `start-release.sh A.B.C [--no-push] [--skip-clean-check]` | Cut `release/A.B.C` from develop + version bump | On `develop`, clean tree |
| `gen-changelog-ai.sh A.B.C [--model M]` | AI-generate en+ru fastlane changelogs | `GITHUB_TOKEN` set |
| `finalize-release.sh A.B.C [--no-push] [--skip-clean-check]` | Run gen-changelog-ai, commit, merge to master+develop, tag | On `release/A.B.C`, clean tree, `GITHUB_TOKEN` set, master+develop fetched locally |
| `build-release-artifacts.sh A.B.C [--skip-legacy]` | Build signed Android foss + desktop Linux/Win + web | `keystore.properties` at repo root |
| `gen-release-notes-ai.sh vA.B.C [--model M] [--output PATH]` | AI-generate bilingual markdown release notes | `GITHUB_TOKEN` set |

`GITHUB_TOKEN` for local use: `export GITHUB_TOKEN=$(gh auth token)`. In CI it's auto-provided and the workflow declares `permissions: { models: read }`.

## CI workflows

All workflows live in `.github/workflows/`. The release-flow workflows are thin wrappers that invoke the scripts above with `--skip-clean-check`.

| Workflow | Trigger | What it does |
|---|---|---|
| `ktlint.yml` | push to `master`/`develop`, PR to `master`/`develop`/`release/**`/`hotfix/**` | `./gradlew ktlintCheck buildSrc:ktlintCheck` |
| `test.yml` | same as ktlint | Runs `./gradlew testCore`, uploads test reports on failure |
| `android.yml` | same as ktlint | Builds `android:buildFossDebug` as a smoke test |
| `branch-name.yml` | PR opened/edited/sync (same-repo only) | Validates head branch matches conventions for the chosen base |
| `commit-lint.yml` | PR opened/sync (same-repo only) | On `issue/NN` branches, requires every non-merge commit subject to start with `#NN: ` |
| `release-branch.yml` | push or PR on `release/**` / `hotfix/**` | Full multi-platform build (Android foss debug + desktop Linux/Win + web) with mock keystore |
| `start-release.yml` | `workflow_dispatch` | Calls `scripts/start-release.sh` |
| `finalize-release.yml` | `workflow_dispatch` | Calls `scripts/finalize-release.sh` |
| `release.yml` | push of tag `v*.*.*` or `workflow_dispatch` (dispatched by `finalize-release.yml`) | Decodes keystore, calls `scripts/build-release-artifacts.sh`, calls `scripts/gen-release-notes-ai.sh`, publishes the GitHub Release |

### Required secrets

Configure in Settings → Secrets and variables → Actions:

| Secret | Required by | Value |
|---|---|---|
| `ANDROID_KEYSTORE_BASE64` | `release.yml` | `base64 -w0 release.keystore` of your `.jks` / `.keystore` file |
| `ANDROID_KEYSTORE_PASSWORD` | `release.yml` | keystore password |
| `ANDROID_KEY_ALIAS` | `release.yml` | key alias inside the keystore |
| `ANDROID_KEY_PASSWORD` | `release.yml` | key password |
`GITHUB_TOKEN` (provided automatically) covers AI inference, PR creation, auto-merge enablement, and the explicit `workflow_dispatch` of `release.yml`.

### Repo settings

- Settings → General → Pull Requests → ✅ **Allow auto-merge** (required for `finalize-release.yml`).
- Settings → Actions → General → **Workflow permissions** → ✅ "Read and write permissions". This lets `finalize-release.yml` push the changelog commit to the release branch and create PRs.

### Branch protection

The PR-based finalize flow lets you keep full branch protection on `master` and `develop`:

- For `master`: ✅ Require status checks (`Ktlint`, `Tests`, `Android CI`), ✅ Require pull request reviews.
- For `develop`: same.
- Optionally enforce linear history on `develop`.
- The release branch itself (`release/**`) needs to be pushable by `github-actions[bot]` (for the `[skip ci]` changelog commit). Either don't protect `release/**`, or allow the bot to bypass.

Because the bot can't self-approve, you approve the master and develop PRs manually on GitHub once `finalize-release.yml` opens them. Auto-merge then takes over.

If you'd rather not grant bypass, change `finalize-release.yml` to create PRs for the master/develop merges instead of pushing directly — you'll lose one-click automation but keep strict protection.
