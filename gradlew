#!/bin/sh
# Gradle wrapper placeholder — full wrapper jar is fetched by Android Studio on first sync.
# If `gradle` is installed, this delegates to it; otherwise it prints guidance.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle not found. Open this project in Android Studio (it will download Gradle 8.7 automatically),"
  echo "or install Gradle and run: gradle wrapper --gradle-version 8.7"
  exit 1
fi
