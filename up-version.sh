#!/usr/bin/env bash

new_version=$1

sed -i 's/\(const val versionName = \)\".*\"/\1\"'"$new_version"'\"/g' buildSrc/src/main/kotlin/ApplicationInfo.kt
sed -i 's/\(\s*const val versionCode = \)\([0-9]*\)/echo "\1$((\2+1))"/ge' buildSrc/src/main/kotlin/ApplicationInfo.kt
sed -i 's/\(const val VERSION = \)\".*\"/\1\"'"$new_version"'\"/' core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt

git add buildSrc/src/main/kotlin/ApplicationInfo.kt core/common/src/main/kotlin/ru/fredboy/cavedroid/common/CaveDroidConstants.kt
