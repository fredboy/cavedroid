#!/usr/bin/env bash

# Source of truth for changelog / release-notes locales.
#
# Must stay in sync with SUPPORTED_LOCALES in
# core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt.
#
# Intended to be sourced, not executed:
#   source "$(dirname "$0")/locales.sh"

# Ordered list of supported locale codes.
SUPPORTED_LOCALES=(en ru de)

# Locale code -> fastlane directory under fastlane/metadata/android/
declare -A LOCALE_FASTLANE_DIRS=(
  [en]="en-US"
  [ru]="ru-RU"
  [de]="de-DE"
)

# Locale code -> English language name (used in AI prompts that request a translation).
declare -A LOCALE_NAMES=(
  [en]="English"
  [ru]="Russian"
  [de]="German"
)

# Locale code -> autonym (used as section headings in release-notes markdown).
declare -A LOCALE_AUTONYMS=(
  [en]="English"
  [ru]="Русский"
  [de]="Deutsch"
)
