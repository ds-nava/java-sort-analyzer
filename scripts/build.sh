#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$ROOT_DIR/dist"
JAR_FILE="$DIST_DIR/java-sort-analyzer.jar"

if command -v mvn >/dev/null 2>&1 && [[ "${1:-}" != "--no-maven" ]]; then
  echo "[1/3] Compilando e empacotando com Maven (FlatLaf + fonte Inter)..."
  cd "$ROOT_DIR"
  mvn -q clean package

  echo "[2/3] Executando suíte de testes..."
  java -Djava.awt.headless=true -cp "$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes" com.sortanalyzer.SuiteTestes

  echo "[3/3] Copiando JAR executável para dist/..."
  mkdir -p "$DIST_DIR"
  cp "$ROOT_DIR/target/java-sort-analyzer.jar" "$JAR_FILE"
else
  echo "Usando compilação direta com JDK (javac + jar)..."
  DEPS_DIR="$ROOT_DIR/.deps"
  FLATLAF_JAR="$DEPS_DIR/flatlaf-3.5.4.jar"
  INTER_JAR="$DEPS_DIR/flatlaf-fonts-inter-4.1.jar"
  BUILD_MAIN="$ROOT_DIR/build/classes"
  BUILD_TEST="$ROOT_DIR/build/test-classes"
  BUILD_FAT="$ROOT_DIR/build/fatjar"

  mkdir -p "$DEPS_DIR"
  if [[ ! -f "$FLATLAF_JAR" ]]; then
    echo "[0/4] Baixando flatlaf-3.5.4.jar do Maven Central..."
    curl -fsSL -o "$FLATLAF_JAR" "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/flatlaf-3.5.4.jar"
  fi
  if [[ ! -f "$INTER_JAR" ]]; then
    echo "[0/4] Baixando flatlaf-fonts-inter-4.1.jar do Maven Central..."
    curl -fsSL -o "$INTER_JAR" "https://repo1.maven.org/maven2/com/formdev/flatlaf-fonts-inter/4.1/flatlaf-fonts-inter-4.1.jar"
  fi

  echo "[1/4] Limpando diretórios de build..."
  rm -rf "$ROOT_DIR/build" "$DIST_DIR"
  mkdir -p "$BUILD_MAIN" "$BUILD_TEST" "$BUILD_FAT" "$DIST_DIR"

  DEPS_CP="$FLATLAF_JAR:$INTER_JAR"

  echo "[2/4] Compilando código-fonte principal (Java 17)..."
  find "$ROOT_DIR/src/main/java" -name "*.java" > "$ROOT_DIR/build/sources.txt"
  javac --release 17 -encoding UTF-8 -cp "$DEPS_CP" -d "$BUILD_MAIN" @"$ROOT_DIR/build/sources.txt"

  echo "[3/4] Compilando e executando suíte de testes..."
  find "$ROOT_DIR/src/test/java" -name "*.java" > "$ROOT_DIR/build/test-sources.txt"
  javac --release 17 -encoding UTF-8 -cp "$BUILD_MAIN:$DEPS_CP" -d "$BUILD_TEST" @"$ROOT_DIR/build/test-sources.txt"
  java -Djava.awt.headless=true -cp "$BUILD_MAIN:$BUILD_TEST:$DEPS_CP" com.sortanalyzer.SuiteTestes

  echo "[4/4] Gerando JAR executável autocontido (com FlatLaf + Inter)..."
  (
    cd "$BUILD_FAT"
    jar --extract --file "$FLATLAF_JAR"
    jar --extract --file "$INTER_JAR"
    rm -f META-INF/MANIFEST.MF META-INF/*.SF META-INF/*.DSA META-INF/*.RSA
  )
  cp -r "$BUILD_MAIN"/. "$BUILD_FAT"/
  jar --create --file "$JAR_FILE" --main-class com.sortanalyzer.Main -C "$BUILD_FAT" .
fi

echo ""
echo "Build concluído com sucesso!"
echo "JAR executável: $JAR_FILE"
echo "Para abrir a interface gráfica: java -jar \"$JAR_FILE\""
