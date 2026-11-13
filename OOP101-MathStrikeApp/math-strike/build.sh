#!/usr/bin/env bash
# Build Math Strike: Meteoroid Blast with the plain JDK (no Maven/Gradle needed).
#   ./build.sh          compile and package MathStrike.jar
#   ./build.sh test     also run the rule self-tests
#   ./build.sh run      build, then start the game
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out && mkdir -p out/classes
javac -encoding UTF-8 -d out/classes src/main/java/mathstrike/*.java
printf 'Main-Class: mathstrike.MathStrike\n' > out/manifest.txt
jar cfm MathStrike.jar out/manifest.txt -C out/classes .
echo "Built MathStrike.jar"

if [[ "${1:-}" == "test" ]]; then
  mkdir -p out/test
  javac -encoding UTF-8 -cp out/classes -d out/test src/test/java/mathstrike/*.java
  java -Djava.awt.headless=true -cp out/classes:out/test mathstrike.SelfTest
fi

if [[ "${1:-}" == "run" ]]; then
  exec java -jar MathStrike.jar
fi
