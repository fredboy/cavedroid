#!/usr/bin/env bash

# Generates en + ru plaintext changelogs for fastlane via GitHub Models.
#
# Usage:
#   GITHUB_TOKEN=$(gh auth token) ./scripts/gen-changelog-ai.sh A.B.C [--model MODEL]
#
# Writes:
#   fastlane/metadata/android/en-US/changelogs/<versionCode>.txt
#   fastlane/metadata/android/ru-RU/changelogs/<versionCode>.txt
#
# Falls back to raw `git log` (en only, copied to ru) if the AI call fails.

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

prompt="You are generating user-facing changelogs for CaveDroid, a 2D Minecraft-inspired indie game (Android, desktop, web).

Below is the raw git commit log for the upcoming v${version} release. Drop refactors, internal cleanup, build/CI tweaks, and dependency bumps — keep only what a player would care about (new features, gameplay changes, bug fixes, UI improvements).

Output two plaintext changelogs:
  1. English
  2. Russian (idiomatic, not literal)

Each must be under 480 characters. Use simple dash bullet points. NO markdown formatting and NO code fences. Friendly, concise tone. If there are no player-facing changes, write a single bullet: \"- Minor improvements and bug fixes\" / \"- Незначительные улучшения и исправления\".

Format your reply EXACTLY as:
<<<EN>>>
- ...
- ...
<<<RU>>>
- ...
- ...

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

if [[ $use_fallback -eq 0 ]]; then
  # Strip optional leading/trailing code fences (defensive — prompt says no fences)
  content=$(printf '%s\n' "$content" | sed -e '/^```/d')

  en=$(awk '/<<<EN>>>/{flag=1;next} /<<<RU>>>/{flag=0} flag' <<< "$content")
  ru=$(awk '/<<<RU>>>/{flag=1;next} flag' <<< "$content")

  if [[ -z "$en" || -z "$ru" ]]; then
    echo >&2 "warning: AI output missing <<<EN>>> or <<<RU>>> markers — falling back"
    echo >&2 "---- AI output ----"
    echo >&2 "$content"
    echo >&2 "-------------------"
    use_fallback=1
  fi
fi

if [[ $use_fallback -eq 1 ]]; then
  fallback=$(printf '%s\n' "$commits" | head -n 10)
  en="$fallback"
  ru="$fallback"
fi

version_code=$(grep 'const val versionCode' buildSrc/src/main/kotlin/ApplicationInfo.kt | sed -E 's/.*versionCode = ([0-9]+)/\1/')

if [[ -z "$version_code" ]]; then
  echo >&2 "error: could not parse versionCode from ApplicationInfo.kt"
  exit 1
fi

en_dir="fastlane/metadata/android/en-US/changelogs"
ru_dir="fastlane/metadata/android/ru-RU/changelogs"
mkdir -p "$en_dir" "$ru_dir"

printf '%s\n' "$en" > "$en_dir/${version_code}.txt"
printf '%s\n' "$ru" > "$ru_dir/${version_code}.txt"

echo ">> Wrote $en_dir/${version_code}.txt"
echo ">> Wrote $ru_dir/${version_code}.txt"
echo
echo "---- en ----"
cat "$en_dir/${version_code}.txt"
echo
echo "---- ru ----"
cat "$ru_dir/${version_code}.txt"
