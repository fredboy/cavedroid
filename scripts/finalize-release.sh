#!/usr/bin/env bash

# Finalize a release via PRs:
#   1. AI-generate fastlane changelogs (one per supported locale), commit, push release branch.
#   2. Open PR  release/A.B.C -> master  with auto-merge enabled.
#   3. Wait for it to merge (manual approval expected on the PR side).
#   4. Tag vA.B.C on the master merge commit, push the tag.
#   5. Open PR  release/A.B.C -> develop  with auto-merge enabled.
#   6. Dispatch the Release workflow for the new tag.
#
# Auto-merge fires once required status checks pass *and* the PR has any
# required reviews. The script does not approve the PRs — GitHub forbids
# a PR author from approving their own PR, and we deliberately rely on a
# human review step.
#
# Usage:
#   GITHUB_TOKEN=$(gh auth token) ./scripts/finalize-release.sh A.B.C [flags]
#
# Flags:
#   --skip-clean-check     skip the clean-work-tree guard (used in CI)
#   --no-wait              open the master PR with auto-merge but don't wait
#                          (no tag, no develop PR, no release dispatch — you
#                          finish those steps manually). Useful for local use
#                          when you want to step away.
#   --timeout SECONDS      how long to wait for the master PR to merge.
#                          Default: 1800 (30 minutes).

set -euo pipefail

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root"
  exit 1
fi

if [[ -z "${1:-}" ]]; then
  echo "usage: $0 A.B.C [--skip-clean-check] [--no-wait] [--timeout SECONDS]"
  exit 1
fi

version=$1
shift || true

clean_check=1
wait_for_merge=1
timeout_seconds=1800
while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-clean-check) clean_check=0;     shift ;;
    --no-wait)          wait_for_merge=0;  shift ;;
    --timeout)          timeout_seconds=$2; shift 2 ;;
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

if git show-ref --verify --quiet "refs/tags/$tag"; then
  echo >&2 "error: tag '$tag' already exists locally"
  exit 1
fi

if git ls-remote --exit-code --tags origin "$tag" >/dev/null 2>&1; then
  echo >&2 "error: tag '$tag' already exists on origin"
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo >&2 "error: 'gh' CLI is required (install it and run 'gh auth login')"
  exit 1
fi

echo ">> Generating AI changelog (fastlane, one per supported locale)"
./scripts/gen-changelog-ai.sh "$version"

version_code=$(grep 'const val versionCode' buildSrc/src/main/kotlin/ApplicationInfo.kt | sed -E 's/.*versionCode = ([0-9]+)/\1/')

echo ">> Committing fastlane changelog"
shopt -s nullglob
changelog_files=(fastlane/metadata/android/*/changelogs/"${version_code}.txt")
shopt -u nullglob
if [[ ${#changelog_files[@]} -eq 0 ]]; then
  echo >&2 "error: no changelog files were produced for versionCode ${version_code}"
  exit 1
fi
git add "${changelog_files[@]}"
git commit -m "[skip ci] Add changelog for $tag"

echo ">> Pushing $branch"
git push origin "$branch"

echo ">> Creating PR: $branch -> master"
master_pr_url=$(gh pr create \
  --base master \
  --head "$branch" \
  --title "Release $tag" \
  --body "Automated release PR. Merges \`$branch\` into \`master\`.

When this PR merges, \`$tag\` will be tagged on the master merge commit and the Release workflow will publish signed artifacts.")
master_pr_num="${master_pr_url##*/}"
echo ">> Master PR: $master_pr_url (#$master_pr_num)"

echo ">> Enabling auto-merge on master PR"
gh pr merge "$master_pr_num" --auto --merge --delete-branch=false

if [[ $wait_for_merge -eq 0 ]]; then
  cat <<EOF

Release $tag staged (no-wait mode).
  - master PR #$master_pr_num created with auto-merge enabled.
  - Approve it on GitHub. Once it merges, finish manually:
      git fetch origin master
      git tag $tag origin/master
      git push origin $tag
      gh pr create --base develop --head $branch \\
        --title "Sync $tag into develop" \\
        --body "Automated post-release sync."
      gh pr merge --auto --merge <develop-pr-number>
      gh workflow run release.yml -f tag=$tag
EOF
  exit 0
fi

echo ">> Waiting up to ${timeout_seconds}s for master PR to merge (approve it on GitHub)..."
elapsed=0
interval=10
state="UNKNOWN"
while [[ $elapsed -lt $timeout_seconds ]]; do
  state=$(gh pr view "$master_pr_num" --json state -q '.state')
  case "$state" in
    MERGED) echo ">> Master PR merged"; break ;;
    CLOSED) echo >&2 "error: master PR #$master_pr_num was closed without merging"; exit 1 ;;
  esac
  sleep "$interval"
  elapsed=$((elapsed + interval))
done
if [[ "$state" != "MERGED" ]]; then
  echo >&2 "error: master PR #$master_pr_num did not merge within ${timeout_seconds}s"
  echo >&2 "  approve and merge it manually, then re-run with --no-wait or finish the remaining steps by hand"
  exit 1
fi

echo ">> Fetching master and tagging $tag on the merge commit"
git fetch origin master
git tag "$tag" origin/master
git push origin "$tag"

echo ">> Creating PR: $branch -> develop"
develop_pr_url=$(gh pr create \
  --base develop \
  --head "$branch" \
  --title "Sync $tag into develop" \
  --body "Automated post-release sync. Merges \`$branch\` into \`develop\` after \`$tag\` was released.")
develop_pr_num="${develop_pr_url##*/}"
echo ">> Develop PR: $develop_pr_url (#$develop_pr_num)"

echo ">> Enabling auto-merge on develop PR"
gh pr merge "$develop_pr_num" --auto --merge --delete-branch=false

echo ">> Dispatching Release workflow"
gh workflow run release.yml -f tag="$tag"

cat <<EOF

Release $tag finalized.
  - master PR #$master_pr_num merged
  - $tag tagged on master
  - develop PR #$develop_pr_num created with auto-merge enabled (approve to sync)
  - Release workflow dispatched for $tag
EOF
