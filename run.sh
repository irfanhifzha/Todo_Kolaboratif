#!/bin/bash
# Compiles (if needed) and runs the Class App.
set -e
mkdir -p out
javac -cp lib/sqlite-jdbc-3.53.2.0.jar -d out $(find src -name "*.java")
java -cp "out:lib/sqlite-jdbc-3.53.2.0.jar" com.classapp.Main
