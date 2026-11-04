---
name: cavedroid-add-locale
description: Add a new CaveDroid UI language, including game bundles, the language picker, and store metadata. Not needed for individual translation fixes.
---

# Add a CaveDroid locale

Choose the language code, Fastlane directory, English name, and native display
name from the request. Infer a reasonable regional variant when unspecified.

## Files to update

- `core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt`:
  add the locale to `SUPPORTED_LOCALES`.
- `scripts/locales.sh`: update `SUPPORTED_LOCALES`, `LOCALE_FASTLANE_DIRS`, and
  `LOCALE_NAMES`. Keep the Kotlin and shell lists in the same order, English first;
  preserve the relative order of existing entries.
- `assets/i18n/`: add `CaveDroid_Items_<code>.properties`,
  `CaveDroid_Menu_<code>.properties`, and `CaveDroid_Onboarding_<code>.properties`.
  Translate from the English bundles, preserving keys, placeholders, and escapes.
- `core/common/src/main/kotlin/ru/fredboy/cavedroid/common/utils/GenericUtils.kt`:
  add the autonym to `Locale.nativeDisplayName()`.
- `fastlane/metadata/android/<locale-dir>/`: translate `title.txt`,
  `short_description.txt`, and `full_description.txt` from `en-US`.
  Changelog directories and new version entries are created by the generator;
  do not backfill past releases.

All platforms use the shared bundles in `assets/`; no separate launcher or web
localization wiring is needed.

## Verification

Compare each new bundle's key set and placeholders with English. Confirm that
the Kotlin and shell locale lists match, and that the picker has the native name.
Follow [AGENTS.md](../../../AGENTS.md) for build and verification conventions.
