#!/usr/bin/env bash
# Compiles the project without Maven, using the sqlite-jdbc jar bundled in lib/.
set -e
cd "$(dirname "$0")"

mkdir -p out
javac -encoding UTF-8 -cp "lib/sqlite-jdbc-3.36.0.3.jar" -d out $(find src/main/java -name "*.java")

echo "Compiled to ./out"
