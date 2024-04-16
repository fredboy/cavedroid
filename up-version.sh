#!/usr/bin/env bash

git stash > /dev/null
stashed=$?

new_version=$1

new_version_string=$(echo $new_version | sed 's/\(alpha\|beta\)\(.*\)/\1 \2/')

sed -i 's/\(version\s=\s\)'"'"'.*'"'"'/\1'"'"''"$new_version"''"'"'/g' build.gradle
sed -i 's/\(versionName\s\)\".*\"/\1\"'"$new_version"'\"/g' android/build.gradle
sed -i 's/\(^\s*versionCode\s\)\([0-9]*\)/echo "\1$((\2+1))"/ge' android/build.gradle
sed -i 's/\(public static final String VERSION = \)\".*\"/\1\"'"$new_version_string"'\"/' core/src/ru/deadsoftware/cavedroid/CaveGame.java

git add build.gradle android/build.gradle core/src/ru/deadsoftware/cavedroid/CaveGame.java

git commit -m "Update version"
git tag "$new_version"

if [ $stashed ]; then
  git stash pop
fi
