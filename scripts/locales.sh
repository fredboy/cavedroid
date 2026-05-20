#!/usr/bin/env bash

# Source of truth for changelog / release-notes locales.
#
# Must stay in sync with SUPPORTED_LOCALES in
# core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt.
#
# Intended to be sourced, not executed:
#   source "$(dirname "$0")/locales.sh"

# Ordered list of supported locale codes.
SUPPORTED_LOCALES=(en es de ru)

# Locale code -> fastlane directory under fastlane/metadata/android/
declare -A LOCALE_FASTLANE_DIRS=(
  [en]="en-US"
  [es]="es-ES"
  [de]="de-DE"
  [ru]="ru-RU"
)

# Locale code -> English language name (used in AI prompts that request a translation).
declare -A LOCALE_NAMES=(
  [en]="English"
  [es]="Spanish"
  [de]="German"
  [ru]="Russian"
)

# Locale code -> autonym (used as section headings in release-notes markdown).
declare -A LOCALE_AUTONYMS=(
  [en]="English"
  [es]="Español"
  [de]="Deutsch"
  [ru]="Русский"
)
