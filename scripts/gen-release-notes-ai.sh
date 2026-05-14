#!/usr/bin/env bash

# Generates bilingual (en + ru) markdown release notes via GitHub Models.
#
# Usage:
#   GITHUB_TOKEN=$(gh auth token) ./scripts/gen-release-notes-ai.sh vA.B.C [--model MODEL] [--output PATH]
#
# Writes to stdout by default, or to --output PATH if given.
# Falls back to a raw `git log` markdown body if the AI call fails.

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root"
  exit 1
fi

if [[ -z "${1:-}" ]]; then
  echo "usage: $0 vA.B.C [--model MODEL] [--output PATH]"
  exit 1
fi

tag=$1
shift || true

model="openai/gpt-4o-mini"
output=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --model)  model=$2;  shift 2 ;;
    --output) output=$2; shift 2 ;;
    *) echo >&2 "unknown arg: $1"; exit 1 ;;
  esac
done

if [[ ! "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo >&2 "error: tag must be vA.B.C (got '$tag')"
  exit 1
fi

if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  echo >&2 "error: GITHUB_TOKEN is required"
  echo >&2 "  in CI:    workflow needs 'permissions: { models: read }' (token is auto-provided)"
  echo >&2 "  locally:  export GITHUB_TOKEN=\$(gh auth token)"
  exit 1
fi

# Previous tag = next-most-recent tag, excluding the current one
prev_tag=$(git tag --sort=-creatordate | grep -v "^${tag}$" | head -n 1 || true)

# Compute commit range — accept either an existing tag or HEAD if the tag
# isn't materialised yet (e.g., when generating notes pre-tag).
if git rev-parse -q --verify "$tag^{commit}" >/dev/null; then
  range_end=$tag
else
  range_end=HEAD
fi

if [[ -n "$prev_tag" ]]; then
  commits=$(git log "$prev_tag".."$range_end" --pretty=format:'- %s' --reverse | grep -v '^- Merge' || true)
else
  commits=$(git log "$range_end" --pretty=format:'- %s' --reverse | grep -v '^- Merge' || true)
fi

write() {
  if [[ -n "$output" ]]; then
    printf '%s\n' "$1" > "$output"
    echo >&2 ">> wrote $output"
  else
    printf '%s\n' "$1"
  fi
}

fallback_body() {
  if [[ -n "$prev_tag" ]]; then
    printf '## Changes since %s\n\n%s\n' "$prev_tag" "$commits"
  else
    printf '## Changes\n\n%s\n' "$commits"
  fi
}

if [[ -z "$commits" ]]; then
  echo >&2 "warning: no commits between ${prev_tag:-(root)} and ${range_end}"
  write "$(fallback_body)"
  exit 0
fi

prompt="You are writing GitHub Release notes for CaveDroid, a 2D Minecraft-inspired indie game (Android, desktop, web).

Below is the raw git commit log for release ${tag} (previous tag: ${prev_tag:-(none)}). Drop refactors, internal cleanup, build/CI tweaks, and dependency bumps — keep only what a player would care about (new features, gameplay changes, bug fixes, UI improvements).

Group items by category (New Features / Improvements / Fixes — omit empty groups). Produce a bilingual markdown body with two top-level sections:

## English

[markdown bullet lists, optionally under #### subheaders per category]

## Русский

[same content, idiomatic translation, same structure]

End the body with an italicised \"Full commit log:\" line linking to the GitHub compare view between ${prev_tag:-(none)} and ${tag} (omit if there is no previous tag).

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

if [[ -z "$content" ]]; then
  echo >&2 "warning: AI release-notes generation failed — falling back to raw git log"
  echo >&2 "API response (first 500 chars): ${response:0:500}"
  write "$(fallback_body)"
  exit 0
fi

write "$content"
