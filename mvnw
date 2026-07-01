#!/usr/bin/env sh

set -e

MAVEN_VERSION=3.9.9
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_HOME="$BASE_DIR/.mvn/apache-maven-$MAVEN_VERSION"
MAVEN_CMD="$MAVEN_HOME/bin/mvn"
MAVEN_ZIP="$BASE_DIR/.mvn/apache-maven-$MAVEN_VERSION-bin.zip"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"

if [ ! -x "$MAVEN_CMD" ]; then
  echo "Maven $MAVEN_VERSION was not found locally. Downloading it now..."
  mkdir -p "$BASE_DIR/.mvn"

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$MAVEN_URL" -o "$MAVEN_ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$MAVEN_URL" -O "$MAVEN_ZIP"
  else
    echo "Neither curl nor wget is available to download Maven." >&2
    exit 1
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$MAVEN_ZIP" -d "$BASE_DIR/.mvn"
  else
    echo "unzip is required to extract Maven." >&2
    exit 1
  fi

  rm -f "$MAVEN_ZIP"
fi

exec "$MAVEN_CMD" "$@"
