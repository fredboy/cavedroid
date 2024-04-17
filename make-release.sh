#!/usr/bin/env bash

if [[ ! $1 ]]; then
  echo "usage: $0 versionName"
  exit
fi

./require_clean_work_tree "$0"

release_dir="release-$1"

mkdir "$release_dir"

./up-version.sh "$1"
./gen-changelog.sh > "$release_dir/CHANGELOG"

./gradlew clean android:assembleRelease desktop:dist

cp android/build/outputs/apk/release/*.apk "$release_dir/"
cp desktop/build/libs/*.jar "$release_dir/"

echo "$release_dir/"
