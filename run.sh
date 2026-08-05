#!/usr/bin/env bash
# Runs the app without Maven. Run ./compile.sh first.
set -e
cd "$(dirname "$0")"

java -cp "out:lib/sqlite-jdbc-3.36.0.3.jar" com.classapp.Main
