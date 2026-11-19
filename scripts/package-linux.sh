#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT_DIR/scripts/build.sh"

OUT_DIR="$ROOT_DIR/dist/package-linux"
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

echo "Empacotando aplicativo Linux com Java embutido via jpackage (app-image)..."
jpackage \
  --type app-image \
  --name "JavaSortAnalyzer" \
  --app-version "1.0.0" \
  --vendor "Java Sort Analyzer" \
  --description "Sistema para Analise de Performance e Visualizacao de Algoritmos de Ordenacao" \
  --input "$ROOT_DIR/dist" \
  --main-jar "java-sort-analyzer.jar" \
  --main-class "com.sortanalyzer.Main" \
  --dest "$OUT_DIR"

echo "Pacote portátil criado em: $OUT_DIR/JavaSortAnalyzer"
echo "Executável: $OUT_DIR/JavaSortAnalyzer/bin/JavaSortAnalyzer"
