#!/usr/bin/env bash

set -euo pipefail

if [[ ! $1 ]]; then
  echo "usage: $0 versionName"
  exit
fi

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  java_bin="$JAVA_HOME/bin/java"
else
  java_bin="java"
fi
java_version=$("$java_bin" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')
if [[ "$java_version" != "17" ]]; then
  echo ">> Java 17 required; found '${java_version:-unknown}' via $java_bin (JAVA_HOME=${JAVA_HOME:-(unset)})"
  exit 1
fi
echo ">> Java 17 detected (via $java_bin)"

./require-clean-work-tree.sh "$0" || exit 1

release_dir="release-$1"

echo ">> Creating $release_dir/"
mkdir "$release_dir"

echo ">> Bumping version to $1"
./up-version.sh "$1"

echo ">> Running ktlint + main builds (Android foss release, desktop Linux/Win)"
./gradlew clean ktlintCheck android:assembleFossRelease desktop:packageLinuxX64 desktop:packageWinX64

echo ">> Copying main artifacts to $release_dir/"
cp "android/build/outputs/apk/foss/release/android-foss-release.apk" "$release_dir/android-foss-$1.apk"
cp "desktop/build/dist/cavedroid-linuxX64.zip" "$release_dir/linux-x86_64-$1.zip"
cp "desktop/build/dist/cavedroid-winX64.zip" "$release_dir/win-x86_64-$1.zip"

# Legacy Android build (best-effort). If legacy-migration.patch is present and
# still applies, produce an extra android-legacy-foss-$1.apk for API 16/17
# devices. Failure here is non-fatal: we assume this version does not support
# legacy devices and continue with the release.
if [[ -f legacy-migration.patch ]]; then
  echo ">> Found legacy-migration.patch — attempting legacy Android build"
  patched_files=$(grep -E '^\+\+\+ b/' legacy-migration.patch | sed 's|^+++ b/||' || true)
  if git apply --check legacy-migration.patch 2>/dev/null; then
    git apply legacy-migration.patch
    if ./gradlew clean android:assembleFossRelease; then
      cp "android/build/outputs/apk/foss/release/android-foss-release.apk" \
         "$release_dir/android-legacy-foss-$1.apk"
      echo ">> Legacy build succeeded: $release_dir/android-legacy-foss-$1.apk"
    else
      echo ">> Legacy gradle build failed — release continues without legacy APK"
    fi
    # Revert only the files in the patch — preserves up-version.sh's version
    # bumps, which touch different files than legacy-migration.patch.
    # shellcheck disable=SC2086
    git checkout -- $patched_files
  else
    echo ">> legacy-migration.patch does not apply cleanly — skipping legacy build"
  fi
fi

echo ">> Tagging $1"
git tag "$1"

echo ">> Generating changelog"
./gen-changelog.sh > "$release_dir/CHANGELOG"

version_code=$(grep 'const val versionCode' buildSrc/src/main/kotlin/ApplicationInfo.kt | sed -E 's/.*versionCode = ([0-9]+)/\1/')
cp "$release_dir/CHANGELOG" "fastlane/metadata/android/en-US/changelogs/$version_code.txt"

echo ">> Committing version bump and re-pointing tag $1"
git add .
git commit -m "Update version"
git tag -d "$1"
git tag "$1"

echo ">> Release ready: $release_dir/"
