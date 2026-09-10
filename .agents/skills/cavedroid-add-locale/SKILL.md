---
name: cavedroid-add-locale
description: Use when the user asks to add a new UI language / locale to CaveDroid (e.g. "add French localization", "support Polish", "translate the game into Japanese"). Encodes the five places that have to stay in sync (Kotlin constant, scripts/locales.sh, libGDX i18n properties bundles, fastlane metadata, the nativeDisplayName autonym map), the ordering rule, what to translate from, and the things that intentionally do NOT need a new locale entry.
---

# CaveDroid — add a new locale

## When this applies

User wants a new in-game UI language. Skip for: changing the default locale, fixing a single string in an existing locale, or adding Android `values-*/strings.xml` resources (the project does not localize the launcher name — only one `strings.xml` exists, and `app_name` is the same in every language).

## Inputs to extract from the request

Before editing anything, pin down:

- **ISO 639-1 code** (e.g. `fr`, `pl`, `ja`). Used as the libGDX bundle suffix and as the locale array key everywhere.
- **Fastlane / Play Store directory name** (e.g. `fr-FR`, `pl-PL`, `ja-JP`). Google Play uses BCP-47 with a region; pick the region that matches the user's intent or the most common variant.
- **English name of the language** (e.g. `French`) — used in the AI changelog prompt.
- **Autonym** — the language's name in itself (e.g. `Français`, `Polski`, `日本語`) — used as the markdown section heading if/when release notes ever go multilingual again.

If the user only gave the language name, infer the rest and state your assumption in the response (e.g. "Using `fr` / `fr-FR` / `Français`").

## The five places that have to stay in sync

All five must be updated together. Forgetting any one will silently break either the runtime locale picker, the build, or the release pipeline.

### 1. `core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt`

Append the new `Locale("<code>")` to `SUPPORTED_LOCALES`. The runtime uses this list to validate stored preferences (`ApplicationContextRepositoryImpl`) and to fall back to English if the stored locale is unsupported (`CaveDroidApplication`). A locale missing here is **silently ignored at runtime** even if the bundle files exist.

### 2. `scripts/locales.sh`

Add an entry to **all four** structures, in the same position as in the Kotlin list:

- `SUPPORTED_LOCALES=(...)` — ordered list of codes.
- `LOCALE_FASTLANE_DIRS[<code>]="<fastlane-dir>"` — drives where the AI changelog script writes per-version files.
- `LOCALE_NAMES[<code>]="<English name>"` — used in the AI changelog prompt.
- `LOCALE_AUTONYMS[<code>]="<autonym>"` — kept for markdown headings even though the release-notes script is currently English-only.

Sourced by `scripts/gen-changelog-ai.sh`. A missing entry will fail the changelog generation in `finalize-release.yml`.

### 3. libGDX i18n bundle files under `assets/`

Create three `.properties` files (UTF-8, plain text, `key=value` per line):

- `assets/i18n/CaveDroid_Items_<code>.properties`
- `assets/i18n/CaveDroid_Menu_<code>.properties`
- `assets/i18n/CaveDroid_Onboarding_<code>.properties`

Mirror the **keys exactly** from the `_en.properties` counterparts. Don't add or omit keys — the bundle falls back to English if a key is missing, but reorderings and key renames silently desync the three bundles. `assets/` is symlinked into both `desktop/src/main/resources` and `android/src/main/assets`, so a single write covers all platforms.

Notes on the existing translations:

- The `mushroom_brown` and `mushroom_red` items both translate to a generic "Mushroom" in every existing locale — keep that pattern.
- `{0}` placeholders in `_Menu_` strings are MessageFormat args; preserve them verbatim.
- `\n` is a literal escape sequence interpreted by libGDX — keep it.
- The German file (`CaveDroid_*_de.properties`) and Spanish file (`CaveDroid_*_es.properties`) are the most recent, complete reference translations — use whichever is linguistically closer to the target.

### 4. `core/common/src/main/kotlin/ru/fredboy/cavedroid/common/utils/GenericUtils.kt`

Add a `"<code>" -> "<autonym>"` branch to the `Locale.nativeDisplayName()` `when`. This is what the Settings language picker displays for each locale; the default `else` branch falls back to `Locale.getDisplayLanguage(this).startWithCapital(this)`, which produces something usable but inconsistent with the curated autonyms used everywhere else (notably the autonym must match `LOCALE_AUTONYMS[<code>]` in `scripts/locales.sh`). Keep the branches in `SUPPORTED_LOCALES` order.

### 5. Fastlane metadata under `fastlane/metadata/android/<fastlane-dir>/`

Create exactly:

- `title.txt` — usually `CaveDroid` verbatim, no newline weirdness.
- `short_description.txt` — single line, ~80-char Play Store short description.
- `full_description.txt` — HTML allowed (`<p>`, `<ul>`, `<li>`, `<strong>`), mirrors the structure in `en-US/full_description.txt`.
- `changelogs/` — create the empty directory. **Do not** backfill per-version `<versionCode>.txt` files for past releases; `scripts/gen-changelog-ai.sh` writes the next one on the next release.

These ship to Google Play via fastlane; they do not affect Desktop, iOS, or Web. The `en-US/` directory is canonical — translate from it, not from German.

## Ordering rule

`SUPPORTED_LOCALES` (in both the Kotlin constant and `locales.sh`) is **ordered by playerbase popularity**, English first. When adding a new locale, slot it into the correct position by popularity for the target market — don't append blindly. After adding, the same order must hold in both files.

## What you do NOT need to touch

- `android/src/main/res/values/strings.xml` — only contains `app_name`, shared across all languages by design. No `values-<lang>/` exists or should be created.
- `iOS/` resources — the iOS launcher reads `assets/` directly.
- `html/` (web) — same bundle path; no separate i18n wiring.
- `scripts/gen-release-notes-ai.sh` — English-only by design (see commit history). Do not re-thread `SUPPORTED_LOCALES` through it.
- `buildSrc/` — no per-locale wiring lives here.

## Verification

After editing, eyeball these:

```bash
# All three bundles exist for the new locale and have the same key count as English
for kind in Items Menu Onboarding; do
  diff <(cut -d= -f1 "assets/i18n/CaveDroid_${kind}_en.properties" | sort) \
       <(cut -d= -f1 "assets/i18n/CaveDroid_${kind}_<code>.properties" | sort)
done

# Constant and shell list agree
grep SUPPORTED_LOCALES core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt
grep -E '^SUPPORTED_LOCALES' scripts/locales.sh
```

The key-diff should be empty for all three bundles. The order of codes in the Kotlin and shell lists must match.

## Commit style

Single commit. Subject like `Add <Language> localization` (imperative, ~50 chars). No `Co-Authored-By:` trailer (project convention). The commit should include all five pieces — splitting them creates intermediate broken states (e.g. a locale advertised in the constant with no bundle files, or a curated autonym in `locales.sh` that the Settings picker overrides with libGDX's generic capitalization).

## Things that have bitten past additions

- Adding to the Kotlin list but forgetting `locales.sh` — release builds blow up at `gen-changelog-ai.sh` time.
- Adding to `locales.sh` but forgetting the Kotlin list — the locale is invisible at runtime; the language picker in Settings won't offer it.
- Adding the locale everywhere but forgetting `nativeDisplayName()` — Settings still lists the language, but with libGDX's default `getDisplayLanguage(...)` capitalization instead of the curated autonym (e.g. `Español` vs. `español`).
- Creating `fastlane/metadata/android/<dir>/changelogs/<old-versionCode>.txt` files manually — pointless busywork; the script only writes the upcoming version.
- Translating from German instead of English — German is a translation itself; subtle drift accumulates. Always translate from `en-US/` / `_en.properties`.
