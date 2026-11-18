#!/bin/sh
# Compila todo o projeto para a pasta out/ (Java 8 ou superior)
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")
echo "Compilado em out/"
