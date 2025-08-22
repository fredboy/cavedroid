#!/usr/bin/env bash

if [[ ! $1 ]]; then
  echo "usage: $0 versionName"
  exit
fi

./require-clean-work-tree.sh "$0" || exit 1

release_dir="release-$1"

mkdir "$release_dir"

./up-version.sh "$1"
./gen-changelog.sh > "$release_dir/CHANGELOG"

./gradlew clean ktlintCheck android:assembleRelease desktop:packageLinuxX64 desktop:packageWinX64

cp "android/build/outputs/apk/release/android-release.apk" "$release_dir/android-$1.apk"
cp "desktop/build/dist/cavedroid-linuxX64.zip" "$release_dir/linux-$1.zip"
cp "desktop/build/dist/cavedroid-winX64.zip" "$release_dir/win-$1.zip"

echo "$release_dir/"
