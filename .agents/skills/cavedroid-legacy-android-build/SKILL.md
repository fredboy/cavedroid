---
name: cavedroid-legacy-android-build
description: Build CaveDroid for Android API 16/17 or investigate native-library load failures on those devices.
---

# Legacy Android builds

API 16/17 support is experimental. Lowering `minSdk` alone does not establish
compatibility: native libraries and Android dependencies must also support the
target device. Follow [AGENTS.md](../../../AGENTS.md) for build prerequisites.

## Migration

Use an isolated worktree for the migration so mainline changes and native outputs
stay separate. Inspect `legacy-migration.patch` as a historical starting point:

```bash
git apply --check legacy-migration.patch
```

Apply it only if it passes and the diff still fits the current build. Otherwise,
adapt the migration to current sources. Do not assume a clean apply proves the
patch is complete.

- Earlier migrations used libGDX `1.9.10` and libktx `1.9.10-b6`. Check both `gdx`
  and the separate `gdxBox2d` catalog versions, including their Android natives.
- Lower the FOSS flavor's minimum SDK as needed; keep store dependencies' SDK
  requirements separate. Inspect manifest merger failures and actual API usage
  before overriding a dependency's minimum SDK.
- Resolve API differences from compiler errors. The patch contains examples for
  framebuffer capture, input callbacks, vector angles, and Scene2D/ktx widgets.
- Preserve native extraction dependencies on JNI merge/package tasks. Inspect
  the APK itself rather than relying on staging-directory contents.

## Verification and patch maintenance

Build the requested FOSS variant with `./gradlew :android:assembleFossDebug` or
`:android:assembleFossRelease`. Release builds also exercise shrinking and signing.

Confirm that each intended ABI contains valid `libgdx.so` and `libgdx-box2d.so`
files in the APK before inspecting ELF imports for unavailable symbols such as
`__memcpy_chk`. A missing library or failed extraction is a failure, not evidence
of compatibility. Absence of that one symbol does not prove the app will load.

Test startup and gameplay on the target API/device when available; otherwise
report runtime compatibility as unverified.

If updating the migration patch, include only migration changes and exclude the
patch itself from the diff. Avoid whole-tree restore commands that discard
unrelated work.
