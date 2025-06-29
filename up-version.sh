#!/usr/bin/env bash

new_version=$1

new_version_string=$(echo $new_version | sed 's/\(alpha\|beta\)\(.*\)/\1 \2/')

sed -i 's/\(const val versionName = \)\".*\"/\1\"'"$new_version"'\"/g' buildSrc/src/main/kotlin/ApplicationInfo.kt
sed -i 's/\(\s*const val versionCode = \)\([0-9]*\)/echo "\1$((\2+1))"/ge' buildSrc/src/main/kotlin/ApplicationInfo.kt
sed -i 's/\(const val VERSION = \)\".*\"/\1\"'"$new_version_string"'\"/' core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt

git add buildSrc/src/main/kotlin/ApplicationInfo.kt core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt

git commit -m "Update version"
git tag "$new_version"
q