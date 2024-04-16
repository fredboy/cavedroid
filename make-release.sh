#!/usr/bin/env bash

require_clean_work_tree() {
    # Update the index
    git update-index -q --ignore-submodules --refresh
    err=0

    # Disallow unstaged changes in the working tree
    if ! git diff-files --quiet --ignore-submodules --
    then
        echo >&2 "cannot $1: you have unstaged changes."
        git diff-files --name-status -r --ignore-submodules -- >&2
        err=1
    fi

    # Disallow uncommitted changes in the index
    if ! git diff-index --cached --quiet HEAD --ignore-submodules --
    then
        echo >&2 "cannot $1: your index contains uncommitted changes."
        git diff-index --cached --name-status -r --ignore-submodules HEAD -- >&2
        err=1
    fi

    if [ $err = 1 ]
    then
        echo >&2 "Please commit or stash them."
        exit 1
    fi
}


if [[ ! $1 ]]; then
  echo "usage: $0 versionName"
  exit
fi

require_clean_work_tree "$0"

release_dir="release-$1"

mkdir "$release_dir"

./up-version.sh "$1"
./gen-changelog.sh > "$release_dir/CHANGELOG"

./gradlew clean android:assembleRelease desktop:dist

cp android/build/outputs/apk/release/*.apk "$release_dir/"
cp desktop/build/libs/*.jar "$release_dir/"

echo "$release_dir/"
