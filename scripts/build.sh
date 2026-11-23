#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$ROOT_DIR/dist"
JAR_FILE="$DIST_DIR/java-sort-analyzer.jar"

echo "[1/3] Compilando e empacotando com Maven (inclui FlatLaf + fonte Inter)..."
cd "$ROOT_DIR"
mvn -q clean package

echo "[2/3] Executando suíte de testes..."
java -Djava.awt.headless=true -cp "$ROOT_DIR/target/classes:$ROOT_DIR/target/test-classes" com.sortanalyzer.SuiteTestes

echo "[3/3] Copiando JAR executável para dist/..."
mkdir -p "$DIST_DIR"
cp "$ROOT_DIR/target/java-sort-analyzer.jar" "$JAR_FILE"

echo ""
echo "Build concluído com sucesso!"
echo "JAR executável: $JAR_FILE"
echo "Para abrir a interface gráfica: java -jar \"$JAR_FILE\""
