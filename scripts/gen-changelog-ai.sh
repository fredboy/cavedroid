#!/usr/bin/env bash

# Generates plaintext fastlane changelogs for every supported locale via
# GitHub Models. The list of locales (and their fastlane directory mapping)
# lives in scripts/locales.sh.
#
# Usage:
#   GITHUB_TOKEN=$(gh auth token) ./scripts/gen-changelog-ai.sh A.B.C [--model MODEL]
#
# Writes:
#   fastlane/metadata/android/<locale-dir>/changelogs/<versionCode>.txt
#   (one file per entry in SUPPORTED_LOCALES)
#
# Falls back to raw `git log` (same text in every locale) if the AI call fails.

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root"
  exit 1
fi

if [[ -z "${1:-}" ]]; then
  echo "usage: $0 versionName [--model MODEL]"
  exit 1
fi

version=$1
shift || true

model="openai/gpt-4o-mini"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --model) model=$2; shift 2 ;;
    *) echo >&2 "unknown arg: $1"; exit 1 ;;
  esac
done

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo >&2 "error: version must be A.B.C (got '$version')"
  exit 1
fi

if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  echo >&2 "error: GITHUB_TOKEN is required"
  echo >&2 "  in CI:    workflow needs 'permissions: { models: read }' (token is auto-provided)"
  echo >&2 "  locally:  export GITHUB_TOKEN=\$(gh auth token)"
  exit 1
fi

# shellcheck source=scripts/locales.sh
source "$(dirname "$0")/locales.sh"

prev_tag=$(git tag --sort=-creatordate | head -n 1 || true)
if [[ -n "$prev_tag" ]]; then
  commits=$(git log "$prev_tag"..HEAD --pretty=format:'- %s' --reverse | grep -v '^- Merge' || true)
else
  commits=$(git log --pretty=format:'- %s' --reverse | grep -v '^- Merge' || true)
fi

if [[ -z "$commits" ]]; then
  echo >&2 "error: no commits found between ${prev_tag:-(root)} and HEAD"
  exit 1
fi

# Build the AI prompt's "output sections" enumeration dynamically.
locale_list_text=""
output_format_text=""
i=1
for code in "${SUPPORTED_LOCALES[@]}"; do
  name=${LOCALE_NAMES[$code]}
  qualifier=""
  if [[ "$code" != "en" ]]; then
    qualifier=" (idiomatic, not literal)"
  fi
  locale_list_text+="  ${i}. ${name}${qualifier}"$'\n'
  marker_upper=${code^^}
  output_format_text+="<<<${marker_upper}>>>"$'\n'"- ..."$'\n'"- ..."$'\n'
  i=$((i + 1))
done

prompt="You are generating user-facing changelogs for CaveDroid, a 2D Minecraft-inspired indie game (Android, desktop, web).

Below is the raw git commit log for the upcoming v${version} release. Drop refactors, internal cleanup, build/CI tweaks, and dependency bumps — keep only what a player would care about (new features, gameplay changes, bug fixes, UI improvements).

Output one plaintext changelog per language:
${locale_list_text}
Each must be under 480 characters. Use simple dash bullet points. NO markdown formatting and NO code fences. Friendly, concise tone. If there are no player-facing changes, write a single bullet in each language conveying \"Minor improvements and bug fixes\".

Format your reply EXACTLY as:
${output_format_text}
Commits:
${commits}"

request=$(jq -n --arg model "$model" --arg prompt "$prompt" '{
  model: $model,
  messages: [{role: "user", content: $prompt}]
}')

set +e
response=$(curl -sS \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -d "$request" \
  https://models.github.ai/inference/chat/completions)
curl_status=$?
set -e

content=""
if [[ $curl_status -eq 0 ]]; then
  content=$(printf '%s' "$response" | jq -r '.choices[0].message.content // empty')
fi

use_fallback=0
if [[ -z "$content" ]]; then
  echo >&2 "warning: AI changelog generation failed — falling back to raw git log"
  echo >&2 "API response (first 500 chars):"
  echo >&2 "${response:0:500}"
  use_fallback=1
fi

declare -A changelog_by_locale=()

if [[ $use_fallback -eq 0 ]]; then
  # Strip optional leading/trailing code fences (defensive — prompt says no fences)
  content=$(printf '%s\n' "$content" | sed -e '/^```/d')

  # Parse each locale section delimited by <<<XX>>> markers. The next locale's
  # marker (or end-of-content for the last) terminates the current section.
  for idx in "${!SUPPORTED_LOCALES[@]}"; do
    code=${SUPPORTED_LOCALES[$idx]}
    start_marker="<<<${code^^}>>>"
    next=$((idx + 1))
    if [[ $next -lt ${#SUPPORTED_LOCALES[@]} ]]; then
      next_code=${SUPPORTED_LOCALES[$next]}
      end_marker="<<<${next_code^^}>>>"
      section=$(awk -v s="$start_marker" -v e="$end_marker" '
        $0 ~ s { flag=1; next }
        $0 ~ e { flag=0 }
        flag' <<< "$content")
    else
      section=$(awk -v s="$start_marker" '
        $0 ~ s { flag=1; next }
        flag' <<< "$content")
    fi
    if [[ -z "$section" ]]; then
      echo >&2 "warning: AI output missing $start_marker marker — falling back"
      echo >&2 "---- AI output ----"
      echo >&2 "$content"
      echo >&2 "-------------------"
      use_fallback=1
      break
    fi
    changelog_by_locale[$code]=$section
  done
fi

if [[ $use_fallback -eq 1 ]]; then
  fallback=$(printf '%s\n' "$commits" | head -n 10)
  for code in "${SUPPORTED_LOCALES[@]}"; do
    changelog_by_locale[$code]=$fallback
  done
fi

version_code=$(grep 'const val versionCode' buildSrc/src/main/kotlin/ApplicationInfo.kt | sed -E 's/.*versionCode = ([0-9]+)/\1/')

if [[ -z "$version_code" ]]; then
  echo >&2 "error: could not parse versionCode from ApplicationInfo.kt"
  exit 1
fi

for code in "${SUPPORTED_LOCALES[@]}"; do
  dir="fastlane/metadata/android/${LOCALE_FASTLANE_DIRS[$code]}/changelogs"
  mkdir -p "$dir"
  printf '%s\n' "${changelog_by_locale[$code]}" > "$dir/${version_code}.txt"
  echo ">> Wrote $dir/${version_code}.txt"
done

for code in "${SUPPORTED_LOCALES[@]}"; do
  dir="fastlane/metadata/android/${LOCALE_FASTLANE_DIRS[$code]}/changelogs"
  echo
  echo "---- $code ----"
  cat "$dir/${version_code}.txt"
done
