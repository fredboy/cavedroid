---
name: cavedroid-legacy-android-build
description: Use when the user wants to build CaveDroid for Android API 16/17 (Jelly Bean) devices, or when diagnosing the "Couldn't load shared library 'gdx'" / "cannot locate '__memcpy_chk'" crash on old Android. Encodes the libGDX/ktx/box2dlights version downgrade required (the stock 1.12+ natives reference Bionic symbols that don't exist before API 18), every API port the older libraries demand, and the verification step that proves the resulting .so files will actually load.
---

# CaveDroid: building for legacy Android (API 16/17)

## When this applies

Activate on any of:
- "build for android 4.1 / 4.2 / jelly bean"
- "support API 16" / "lower minSdk to 16"
- runtime crash showing `SharedLibraryLoadRuntimeException: Couldn't load shared library 'gdx'` and/or `cannot locate '__memcpy_chk'`
- the user explicitly asks to "downgrade libGDX" for older Android

Skip for: API 18+ targets (the stock libGDX 1.12.x natives load fine on Jelly Bean MR2 and up — no downgrade needed; just set `minSdk = 18`).

## Fast path: apply the saved patch first

**Before touching any source file**, check whether `legacy-migration.patch` exists at the repo root:

```bash
test -f legacy-migration.patch && git apply --check legacy-migration.patch
```

- If both succeed: apply the patch (`git apply legacy-migration.patch`), then jump straight to **Verification step** below. Skip every "API ports" subsection — the patch already contains them.
- If `--check` fails (hunk conflict): the patch is stale against current `master`. Do **not** hand-resolve. Either (a) run the full manual procedure below and regenerate the patch at the end, or (b) confirm with the user before attempting `git apply -3` / `--reject` workarounds.
- If the file doesn't exist: this is the first legacy build, or the patch was deliberately removed. Continue with the manual procedure.

### Is applying the patch enough?

Yes — *if and only if* the **Verification step** passes after a clean `:android:assembleFossDebug`. Concretely, after `git apply`:

1. Build: `./gradlew :android:assembleFossDebug`. A clean compile means the patch's source edits are still compatible with current mainline (no API drift in untouched files).
2. Run the `strings | grep __memcpy_chk` check from the **Verification step**. All zeros means the bundled `libgdx.so` is the 1.9.10 native, which is the actual fix.
3. (Optional cross-check) `file android/libs/armeabi-v7a/libgdx.so` reports `built by NDK r16b`.

If all three pass, the patch is sufficient — no manual ports needed, no other steps required. Install the APK and hand off to the user for device testing.

If the build fails despite a clean `git apply`, the patch is **functionally** stale even though it applied cleanly — mainline added a new file that uses one of the broken APIs (`Pixmap.createFromFrameBuffer`, `setAngleDeg`, `Label.wrap`, `InputProcessor.touchCancelled`, etc.) since the patch was generated. Fall back to the manual procedure for the new call site, then regenerate the patch.

The patch is the source of truth for the legacy migration. The sections below describe what's *in* it — they exist for the case where the patch must be regenerated (mainline drift) or you're auditing what the migration actually does. Do not re-do them by hand when a valid patch is available.

## Root cause (one paragraph)

`__memcpy_chk` is a FORTIFY_SOURCE symbol that Bionic added in **Android 4.3 (API 18)**. libGDX natives from roughly **1.9.11 onwards** were compiled with an NDK that emits this symbol unconditionally, so `libgdx.so` fails to load on API 16/17 regardless of what `minSdk` claims. The last libGDX release whose published Android natives do NOT reference `__memcpy_chk` is **1.9.10** (NDK r16b, "for Android 14"). This is a libGDX-natives problem, not a project-config problem — `minSdk = 16` + `targetSdk = 36` is fine; the binary just doesn't load.

## The downgrade matrix

Edit `buildSrc/src/main/kotlin/Versions.kt`:

```kotlin
const val gdx = "1.9.10"          // was 1.12.1 / 1.13.1
const val libKtx = "1.9.10-b6"    // was 1.13.1-rc1 — must track libGDX major.minor
const val box2dLights = "1.4"     // was 1.5 — 1.5 needs libGDX 1.9.11+
```

Don't bump these one at a time. ktx and box2dlights are coupled to libGDX's ABI; mixing 1.9.10 gdx with 1.13.x ktx will fail at link time, not compile time.

## API ports the downgrade forces

These are the deltas between libGDX 1.12.1 and 1.9.10 that touch this codebase. When porting forward in time again (re-bumping libGDX), reverse them. **Every entry below has already been fixed once** — if you re-run the downgrade after a feature rewrite, search for the old call sites first to confirm whether the fix is still in place.

### `Pixmap.createFromFrameBuffer(x, y, w, h)` — added in 1.10

Replace with the pre-1.10 equivalent:

```kotlin
import com.badlogic.gdx.utils.ScreenUtils
// ...
val pixmap = ScreenUtils.getFrameBufferPixmap(x, y, w, h)
```

Known call sites: `core/data/save/.../SaveDataRepositoryImpl.kt`, `core/gameplay/rendering/.../CrosshairRenderer.kt`.

### `InputProcessor.scrolled(Float, Float)` — was `(Int)` in 1.9.x

The signature changed in 1.10. Revert any override:

```kotlin
override fun scrolled(amount: Int): Boolean {
    // map to the project's mapScrolled(amountX, amountY) by passing 0f, amount.toFloat()
}
```

### `InputProcessor.touchCancelled(...)` — added in 1.11

Remove the override entirely. The base interface in 1.9.10 doesn't declare it.

### `Vector2.setAngleDeg(...)` / `angleDeg()` — added in 1.10

In 1.9.10 the methods are just `setAngle(deg: Float)` and `angle(): Float` (both already use degrees). Rename call sites; no semantic change.

### `MathUtils.atan(x)` — added in 1.10

Use `kotlin.math.atan` directly:

```kotlin
val rotation = kotlin.math.atan((v / h).toDouble()).toFloat() * MathUtils.radDeg
```

Don't reach for `MathUtils.atan2(v, h)` as a substitute — it has different quadrant behavior than `atan(v/h)`.

### `Label.wrap = true` — field is private in 1.9.10

The Kotlin synthetic-property shortcut fails because 1.9.10's `Label` has no public `getWrap()`. Use the explicit setter:

```kotlin
label("...") { setWrap(true) }
```

### ktx `image(texture: Texture)` — added in ktx ~1.10+

ktx 1.9.10-b6's `image(...)` DSL only accepts a drawable name (`String`). For raw Textures, construct the Image manually inside the parent container:

```kotlin
val img = Image(TextureRegionDrawable(TextureRegion(texture))).apply {
    touchable = Touchable.disabled
}
add(img).width(80f).height(80f)
```

Known call site: `core/gdx/.../menu/v2/view/singleplayer/SinglePlayerMenuView.kt` (the save thumbnail). String-form `image("name")` calls elsewhere (e.g. `image("gamelogo")` in `AboutMenuView.kt`) still work — only `image(Texture)` needs porting. A grep for `image(.*screenshot\|image(.*Texture` finds the failing form.

### `proguard-rules.pro` — silence libGDX 1.9.10's stale jnigen refs

`assembleFossDebug` doesn't run R8, but `assembleFossRelease` does — and R8 fails with `Missing class com.badlogic.gdx.jnigen.AntScriptGenerator` (and several siblings) referenced from `com.badlogic.gdx.physics.box2d.utils.Box2DBuild.main()`. `Box2DBuild` is a build-tool main method that's never invoked at runtime, but R8 still resolves its call graph. Newer libGDX versions stripped these references; 1.9.10 still has them.

Add to `android/proguard-rules.pro`:

```proguard
-dontwarn com.badlogic.gdx.jnigen.**
```

`make-release.sh` invokes the release build, so the legacy step will hit this. The rule is harmless on mainline (no warnings to suppress when the classes resolve), so it could just as well live there — but keeping it in the patch keeps the legacy concern self-contained.

### `AndroidManifest.xml` — override kermit's minSdk 21

`android/build.gradle.kts` carries `minSdk = 16`, but AGP's manifest merger fails because `co.touchlab:kermit-android-debug:2.0.8` and `co.touchlab:kermit-core-android-debug:2.0.8` both declare `minSdkVersion="21"` in their bundled manifests. Kermit is pure Kotlin (a multiplatform logger) — the minSdk floor is conservative metadata, not a real native dependency, so the override is safe.

Add to `android/src/main/AndroidManifest.xml` directly under `<manifest>`:

```xml
<uses-sdk tools:overrideLibrary="co.touchlab.kermit,co.touchlab.kermit.core" />
```

Both package names are required — AGP fails the merge for each library separately, so a single-entry override will produce a second identical-looking error after the first is fixed. Don't try to set `android:minSdkVersion` here; AGP rejects that when `defaultConfig.minSdk` is also set. The `xmlns:tools="http://schemas.android.com/tools"` declaration is already on the root `<manifest>` element.

### `box2dLight.Light.direction` — protected field in box2dlights 1.4

box2dlights 1.5 exposed a public setter; 1.4 didn't. Use the package-private accessor pattern already established in `core/common/src/main/kotlin/box2dLight/DirectionalLightExtention.kt`:

```kotlin
package box2dLight

fun Light.setDirectionCompat(direction: Float) {
    this.direction = direction
}
```

Then call sites: `sight.setDirectionCompat(0f)` instead of `sight.direction = 0f`. The trick is that Kotlin top-level functions in package `box2dLight` get JVM package-private access — same mechanism `publicUpdate()` uses to call protected `Light.update()`.

## Verification step (do this, don't skip)

Compile alone proves nothing — the bug we're fixing is a *load-time* failure. After a successful `:android:assembleFossDebug`, confirm the bundled natives don't reference the broken symbol:

```bash
for arch in armeabi-v7a arm64-v8a x86 x86_64; do
  echo "=== $arch ==="
  strings android/libs/$arch/libgdx.so | grep -c __memcpy_chk
done
```

Every line must print `0`. A non-zero count means the natives configuration is still resolving to a newer libGDX version — check `buildSrc/Versions.kt` and the Gradle dependency tree (`./gradlew :android:dependencies | grep gdx-platform`).

Cross-check the NDK signature too:

```bash
file android/libs/armeabi-v7a/libgdx.so
# expect: built by NDK r16b ... for Android 14
```

NDK r16b = libGDX 1.9.10. Anything newer (r19c, r21+) means the wrong artifact was bundled.

## Save the migration as a patch (do this before reverting)

Once the build is verified, capture the full migration diff to `legacy-migration.patch` at the repo root. This turns the downgrade into a portable, reproducible artifact: mainline development continues on `master`, and any future legacy release just re-applies the patch.

```bash
git diff -- ':!legacy-migration.patch' > legacy-migration.patch
```

The `':!legacy-migration.patch'` pathspec is required when the patch already exists in `HEAD` — the redirect truncates the destination *before* `git diff` runs, so a naive `git diff > legacy-migration.patch` captures the patch's own deletion + rewrite, producing a self-referential patch that no longer applies cleanly. Exclude the patch file from the diff to avoid this.

Sanity-check the patch covers what you expect — at minimum it should touch `buildSrc/.../Versions.kt`, `android/build.gradle.kts` (the `minSdk` lowering), `android/src/main/AndroidManifest.xml` (the `<uses-sdk tools:overrideLibrary="co.touchlab.kermit,co.touchlab.kermit.core" />` line), `android/proguard-rules.pro` (the `-dontwarn com.badlogic.gdx.jnigen.**` line), the `box2dLight/DirectionalLightExtention.kt` helper, and every source file listed under "API ports the downgrade forces" above. If any of those are missing, the working tree was not in the right state when you ran `git diff`.

Optional but recommended: after capturing the patch, revert the code/build edits with `git restore` (preserving only `legacy-migration.patch`, the skill, and the README sections). The next legacy release runs:

```bash
git apply legacy-migration.patch
./gradlew :android:assembleFossRelease   # or assembleFossDebug
# (verify libgdx.so as above)
git restore -SW :/                       # back to mainline
```

Keep `legacy-migration.patch` versioned next to release artifacts — it must be re-generated whenever mainline changes a file the patch touches (KSP/automultibind regen, ktx/scene2d API drift, new input handlers). If `git apply` fails with a hunk conflict, the patch is stale: regenerate it from a fresh downgrade attempt rather than hand-resolving.

## What this downgrade does NOT cover

- **Runtime behavior.** Six years of libGDX changes between 1.9.10 and 1.12.1 mean Scene2D layout edge cases, Stage/SpriteBatch blending defaults, Box2D contact-filter ordering, etc., may behave subtly differently. The user must verify on the actual API 16/17 device.
- **Desktop and iOS launchers.** They share `Versions.kt`, so they'll pick up the downgrade automatically and may need their own per-platform API patches.
- **`store` flavor.** Yandex Mobile Ads 8.x targets newer Android. If `:android:assembleStoreDebug` fails after the libGDX downgrade, either downgrade Yandex SDK or constrain the store flavor to a higher minSdk via flavor-specific `defaultConfig { minSdk = 21 }`.
- **`@Deprecated` warnings.** `Vector2.setAngle` and friends will emit deprecation warnings against newer libGDX docs; ignore them — they're correct for 1.9.10.

## Quick scope-check before starting

If the user says "I want API 16" without having tried it yet, push back first: `minSdk = 18` (Jelly Bean MR2, 2013) is the practical floor without a downgrade, and Play Store stats show essentially 0% of users on lower. The downgrade is correct when the user has a specific legacy device they're testing on, or is preserving compatibility with an existing install base on API 16/17.
