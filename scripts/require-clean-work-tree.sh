#!/usr/bin/env bash

# VonC CC BY-SA 4.0
# https://stackoverflow.com/a/3879077
# https://creativecommons.org/licenses/by-sa/4.0/

if [[ ! -f gradlew || ! -f settings.gradle.kts ]]; then
  echo >&2 "error: must be run from the repository root (no ./gradlew here)"
  exit 1
fi

git update-index -q --ignore-submodules --refresh
err=0

if ! git diff-files --quiet --ignore-submodules --
then
    echo >&2 "cannot ${1:-proceed}: you have unstaged changes."
    git diff-files --name-status -r --ignore-submodules -- >&2
    err=1
fi

if ! git diff-index --cached --quiet HEAD --ignore-submodules --
then
    echo >&2 "cannot ${1:-proceed}: your index contains uncommitted changes."
    git diff-index --cached --name-status -r --ignore-submodules HEAD -- >&2
    err=1
fi

if [ $err = 1 ]
then
    echo >&2 "Please commit or stash them."
    exit 1
fi
