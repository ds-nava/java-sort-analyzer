#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_MAIN="$ROOT_DIR/build/classes"
BUILD_TEST="$ROOT_DIR/build/test-classes"
DIST_DIR="$ROOT_DIR/dist"
JAR_FILE="$DIST_DIR/java-sort-analyzer.jar"

echo "[1/4] Limpando diretórios de build..."
rm -rf "$ROOT_DIR/build" "$DIST_DIR"
mkdir -p "$BUILD_MAIN" "$BUILD_TEST" "$DIST_DIR"

echo "[2/4] Compilando código-fonte principal (Java 17)..."
find "$ROOT_DIR/src/main/java" -name "*.java" > "$ROOT_DIR/build/sources.txt"
javac --release 17 -encoding UTF-8 -d "$BUILD_MAIN" @"$ROOT_DIR/build/sources.txt"

echo "[3/4] Compilando e executando suíte de testes..."
find "$ROOT_DIR/src/test/java" -name "*.java" > "$ROOT_DIR/build/test-sources.txt"
javac --release 17 -encoding UTF-8 -cp "$BUILD_MAIN" -d "$BUILD_TEST" @"$ROOT_DIR/build/test-sources.txt"
java -Djava.awt.headless=true -cp "$BUILD_MAIN:$BUILD_TEST" com.sortanalyzer.SuiteTestes

echo "[4/4] Gerando JAR executável..."
jar --create --file "$JAR_FILE" --main-class com.sortanalyzer.Main -C "$BUILD_MAIN" .

echo ""
echo "Build concluído com sucesso!"
echo "JAR executável: $JAR_FILE"
echo "Para abrir a interface gráfica: java -jar \"$JAR_FILE\""
