#!/usr/bin/env bash

# One-time setup of repository settings + rulesets needed by the release flow.
# Idempotent: safe to re-run. Requires `gh auth login` (admin scope on the repo).
#
# What this configures:
#   - Allow squash merging (default for feature PRs to develop).
#   - Allow merge commits (required by finalize-release.sh for release branch
#     -> master / develop sync PRs).
#   - Disable rebase merging.
#   - Enable auto-merge (required by finalize-release.sh).
#   - Don't auto-delete head branches (release branches must be kept).
#   - Squash merge commit message = PR title only.
#   - Merge commit message = PR title.
#   - Ruleset on `release/*` branches: PRs targeting release branches must be
#     squash-merged (enforces "release fixes are squashed").
#
# `develop` is intentionally left flexible — it receives both squash merges
# (from feature PRs) and merge commits (from the release-branch sync PR), and
# GitHub's merge-method restriction is per-target-branch, not per-source-branch.

set -euo pipefail

if ! command -v gh >/dev/null 2>&1; then
  echo >&2 "error: gh CLI is required"
  exit 1
fi

repo=$(gh repo view --json nameWithOwner -q .nameWithOwner)
echo ">> Repo: $repo"

echo
echo ">> Configuring PR settings"
gh api -X PATCH "repos/$repo" \
  -F allow_squash_merge=true \
  -F allow_merge_commit=true \
  -F allow_rebase_merge=false \
  -F allow_auto_merge=true \
  -F delete_branch_on_merge=false \
  -f squash_merge_commit_title=PR_TITLE \
  -f squash_merge_commit_message=BLANK \
  -f merge_commit_title=PR_TITLE \
  -f merge_commit_message=BLANK \
  >/dev/null
cat <<'EOF'
    allow_squash_merge       = true
    allow_merge_commit       = true
    allow_rebase_merge       = false
    allow_auto_merge         = true
    delete_branch_on_merge   = false
    squash commit title      = PR_TITLE
    squash commit message    = BLANK
    merge  commit title      = PR_TITLE
    merge  commit message    = BLANK
EOF

ruleset_name="release branches require squash"

echo
existing_id=$(gh api "repos/$repo/rulesets" --jq ".[] | select(.name == \"$ruleset_name\") | .id" 2>/dev/null || true)

ruleset_body=$(cat <<JSON
{
  "name": "$ruleset_name",
  "target": "branch",
  "enforcement": "active",
  "conditions": {
    "ref_name": {
      "include": ["refs/heads/release/*"],
      "exclude": []
    }
  },
  "rules": [
    {
      "type": "pull_request",
      "parameters": {
        "required_approving_review_count": 0,
        "dismiss_stale_reviews_on_push": false,
        "require_code_owner_review": false,
        "require_last_push_approval": false,
        "required_review_thread_resolution": false,
        "allowed_merge_methods": ["squash"]
      }
    }
  ]
}
JSON
)

if [[ -n "$existing_id" ]]; then
  echo ">> Ruleset '$ruleset_name' already exists (id $existing_id) — updating"
  printf '%s' "$ruleset_body" | gh api -X PUT "repos/$repo/rulesets/$existing_id" --input - >/dev/null
else
  echo ">> Creating ruleset '$ruleset_name'"
  printf '%s' "$ruleset_body" | gh api -X POST "repos/$repo/rulesets" --input - >/dev/null
fi
echo "    target:  refs/heads/release/*"
echo "    rule:    pull_request, allowed_merge_methods=[squash]"

echo
echo ">> Done."
echo
echo "Note: branch protection on master/develop is not configured by this script."
echo "      See RELEASING.md for the recommended ruleset (require PRs + status checks)."
