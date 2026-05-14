#!/usr/bin/env bash

# Finalize a release: AI changelog, commit, merge to master+develop, tag.
# Usable locally or from a CI workflow.
#
# Usage:
#   GITHUB_TOKEN=$(gh auth token) ./scripts/finalize-release.sh A.B.C [--no-push] [--skip-clean-check]

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root"
  exit 1
fi

if [[ -z "${1:-}" ]]; then
  echo "usage: $0 A.B.C [--no-push] [--skip-clean-check]"
  exit 1
fi

version=$1
shift || true

push=1
clean_check=1
while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-push)          push=0;        shift ;;
    --skip-clean-check) clean_check=0; shift ;;
    *) echo >&2 "unknown arg: $1"; exit 1 ;;
  esac
done

branch="release/$version"
tag="v$version"

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo >&2 "error: version must be A.B.C (got '$version')"
  exit 1
fi

if [[ $clean_check -eq 1 ]]; then
  ./scripts/require-clean-work-tree.sh "finalize-release $version"
fi

current_branch=$(git rev-parse --abbrev-ref HEAD)
if [[ "$current_branch" != "$branch" ]]; then
  echo >&2 "error: must be on '$branch' (currently on '$current_branch')"
  exit 1
fi

for required in master develop; do
  if ! git show-ref --verify --quiet "refs/heads/$required"; then
    echo >&2 "error: '$required' branch not found locally — fetch it first"
    exit 1
  fi
done

if git show-ref --verify --quiet "refs/tags/$tag"; then
  echo >&2 "error: tag '$tag' already exists locally"
  exit 1
fi

if git ls-remote --exit-code --tags origin "$tag" >/dev/null 2>&1; then
  echo >&2 "error: tag '$tag' already exists on origin"
  exit 1
fi

echo ">> Generating AI changelog (fastlane en + ru)"
./scripts/gen-changelog-ai.sh "$version"

version_code=$(grep 'const val versionCode' buildSrc/src/main/kotlin/ApplicationInfo.kt | sed -E 's/.*versionCode = ([0-9]+)/\1/')

echo ">> Committing fastlane changelog"
git add "fastlane/metadata/android/en-US/changelogs/${version_code}.txt" \
        "fastlane/metadata/android/ru-RU/changelogs/${version_code}.txt"
git commit -m "[skip ci] Add changelog for $tag"

echo ">> Merging $branch into master"
git checkout master
git merge --no-ff "$branch" -m "Merge $branch into master"

echo ">> Tagging $tag on master"
git tag "$tag"

echo ">> Merging $branch into develop"
git checkout develop
git merge --no-ff "$branch" -m "Merge $branch into develop"

git checkout "$branch"

if [[ $push -eq 1 ]]; then
  echo ">> Pushing master, develop, $branch, $tag"
  git push origin master develop "$branch" "$tag"
fi

cat <<EOF

Release $tag finalized.

EOF

if [[ $push -eq 1 ]]; then
  echo "The Release workflow is now (or will be) building signed artifacts on the tag push."
else
  echo "Push when ready:"
  echo "  git push origin master develop $branch $tag"
fi
