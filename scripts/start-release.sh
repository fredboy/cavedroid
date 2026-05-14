#!/usr/bin/env bash

# Cut a release branch from develop. Usable locally or from a CI workflow.
#
# Usage:
#   ./scripts/start-release.sh A.B.C [--no-push] [--skip-clean-check]

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
  ./scripts/require-clean-work-tree.sh "start-release $version"
fi

current_branch=$(git rev-parse --abbrev-ref HEAD)
if [[ "$current_branch" != "develop" ]]; then
  echo >&2 "error: must be on 'develop' (currently on '$current_branch')"
  exit 1
fi

if git show-ref --verify --quiet "refs/heads/$branch"; then
  echo >&2 "error: branch '$branch' already exists locally"
  exit 1
fi

if git ls-remote --exit-code --heads origin "$branch" >/dev/null 2>&1; then
  echo >&2 "error: branch '$branch' already exists on origin"
  exit 1
fi

if git show-ref --verify --quiet "refs/tags/$tag"; then
  echo >&2 "error: tag '$tag' already exists locally"
  exit 1
fi

if git ls-remote --exit-code --tags origin "$tag" >/dev/null 2>&1; then
  echo >&2 "error: tag '$tag' already exists on origin"
  exit 1
fi

echo ">> Creating $branch from develop"
git checkout -b "$branch"

echo ">> Bumping version to $version"
./scripts/up-version.sh "$version"

echo ">> Committing version bump"
git commit -m "Bump version to $version"

if [[ $push -eq 1 ]]; then
  echo ">> Pushing $branch"
  git push -u origin "$branch"
fi

cat <<EOF

Release branch $branch is ready.

Next steps:
  1. Stabilization fixes: open PRs into $branch.
  2. When stable, finalize:
       ./scripts/finalize-release.sh $version
     or trigger the 'Finalize Release' GitHub Action with version=$version.
EOF
