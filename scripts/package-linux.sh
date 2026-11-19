#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT_DIR/scripts/build.sh"

INPUT_DIR="$ROOT_DIR/build/jpackage-input"
OUT_DIR="$ROOT_DIR/dist/package-linux"

rm -rf "$INPUT_DIR" "$OUT_DIR"
mkdir -p "$INPUT_DIR" "$OUT_DIR"
cp "$ROOT_DIR/dist/java-sort-analyzer.jar" "$INPUT_DIR/java-sort-analyzer.jar"

echo "Empacotando aplicativo Linux com Java embutido via jpackage (app-image)..."
jpackage \
  --type app-image \
  --name "JavaSortAnalyzer" \
  --app-version "1.0.0" \
  --vendor "Java Sort Analyzer" \
  --description "Sistema para Analise de Performance e Visualizacao de Algoritmos de Ordenacao" \
  --input "$INPUT_DIR" \
  --main-jar "java-sort-analyzer.jar" \
  --main-class "com.sortanalyzer.Main" \
  --dest "$OUT_DIR"

echo "Pacote portátil criado em: $OUT_DIR/JavaSortAnalyzer"
echo "Executável: $OUT_DIR/JavaSortAnalyzer/bin/JavaSortAnalyzer"
