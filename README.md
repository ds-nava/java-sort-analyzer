# Java Sort Analyzer

![Java](https://img.shields.io/badge/Java-17%20LTS-0f766e?style=flat-square&logo=openjdk&logoColor=white)
![UI](https://img.shields.io/badge/UI-Swing%20%2B%20Java%202D-1d4ed8?style=flat-square)
![Dependencies](https://img.shields.io/badge/Algoritmos-Zero%20Deps%20(UI%3A%20FlatLaf)-10b981?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-475569?style=flat-square)

Aplicação desktop em **Java 17 (Swing + Java 2D)** para **análise comparativa de desempenho** e **visualização animada** de algoritmos clássicos de ordenação.

> **Contexto do Projeto:** desenvolvido como projeto acadêmico de **Ciência da Computação**, inspirado no desafio real de **geoprocessamento e catalogação de imagens de satélite da Floresta Amazônica** (~100.000 imagens geradas a cada 24 horas para fiscalização ambiental).

---

## Principais Funcionalidades

- **Análise & Catálogo (Aba 1):**
  - **3 fontes de dados:** geração interna determinística (até 100.000+ imagens com *seed* configurável), importação de arquivos externos `.csv` / `.txt` (detecção automática de catálogo completo ou lista numérica simples + relatório de linhas inválidas) e entrada manual direta.
  - **Medição isolada:** todos os algoritmos ordenam cópias idênticas da mesma base, com cronômetro via `System.nanoTime()` exclusivamente ao redor da ordenação (excluindo leitura, geração, clonagem e exibição) e autoverificação automática.
  - **Tabelas paginadas Antes e Depois:** inspeção de 100.000+ registros sem congelamento da interface.
  - **Gráfico Java 2D nativo:** alternância entre escala linear e logarítmica + exportação em **PNG** de alta resolução e **CSV**.
- **Visualizador Animado (Aba 2):**
  - **Modo Individual:** reprodução passo a passo (`Play`, `Pausar`, `< Passo`, `Passo >`, `Reiniciar`), controle de velocidade, cenários e contadores ao vivo de comparações e movimentações.
  - **Modo Corrida Simultânea:** grade 2×3 com os **5 algoritmos disputando sobre a mesma sequência inicial** e placar de chegada em tempo real.
- **Bateria de Benchmark (Aba 3):**
  - Execução em segundo plano (`SwingWorker`) da matriz de tamanhos (`1.000`, `10.000`, `50.000`, `100.000`) × 4 cenários (*Aleatório*, *Já Ordenado*, *Inverso* e *Semi-Ordenado*) com aquecimento da JVM, barra de progresso, botão cancelar e exportação `.csv` / `.png`.

---

## Algoritmos Implementados

| Algoritmo | Melhor Caso | Caso Médio | Pior Caso | Memória Auxiliar | Estável |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Bubble Sort** *(parada antecipada)* | $O(n)$ | $O(n^2)$ | $O(n^2)$ | $O(1)$ | Sim |
| **Insertion Sort** | $O(n)$ | $O(n^2)$ | $O(n^2)$ | $O(1)$ | Sim |
| **Merge Sort** *(buffer único)* | $O(n \log n)$ | $O(n \log n)$ | $O(n \log n)$ | $O(n)$ | Sim |
| **Quick Sort** *(Mediana de 3 + Hoare)* | $O(n \log n)$ | $O(n \log n)$ | $O(n^2)$ | $O(\log n)$ | Não |
| **Heap Sort** *(iterativo in-place)* | $O(n \log n)$ | $O(n \log n)$ | $O(n \log n)$ | $O(1)$ | Não |

---

## Amostra de Resultados (Benchmark Oficial — `n = 10.000`, média de 3 execuções)

Valores registrados em [`docs/resultados/benchmark_uniforme.csv`](docs/resultados/benchmark_uniforme.csv) (`seed = 42`, critério `ID da Imagem`, *warm-up* prévio da JVM em Linux `amd64` / OpenJDK 21):

| Algoritmo | Aleatório | Já Ordenado | Inversamente Ordenado | Semi-Ordenado (90%) |
| :--- | ---: | ---: | ---: | ---: |
| **Bubble Sort** | 407,04 ms | **0,06 ms** | 340,43 ms | 188,90 ms |
| **Insertion Sort** | 98,91 ms | 0,08 ms | 199,13 ms | 11,19 ms |
| **Merge Sort** | 4,43 ms | 0,11 ms | 1,20 ms | 0,99 ms |
| **Quick Sort (Mediana de 3)** | **2,10 ms** | 0,87 ms | **0,48 ms** | **0,59 ms** |
| **Heap Sort** | 3,16 ms | 2,02 ms | 1,95 ms | 2,20 ms |

> Os tempos absolutos variam conforme o hardware e a JVM utilizada. Para reproduzir no seu computador, utilize a Aba 3 ou o modo `--benchmark` abaixo.

---

## Como Compilar e Executar

### Requisitos
- **JDK 17 ou superior** (não requer bibliotecas externas para os algoritmos ou testes; utiliza apenas `FlatLaf` e `flatlaf-fonts-inter` na interface Swing, baixados automaticamente pelo Maven ou pelos scripts de build).

### Build rápido (apenas JDK — funciona offline)

**Linux / macOS:**
```bash
./scripts/build.sh
java -jar dist/java-sort-analyzer.jar
```

**Windows (Prompt de Comando):**
```bat
scripts\build.bat
java -jar dist\java-sort-analyzer.jar
```

### Build com Maven
```bash
mvn package
java -jar target/java-sort-analyzer.jar
```

### Modo Linha de Comando (CLI)
```bash
# Gerar arquivos de teste externos (1k, 10k, 100k registros + lista simples + arquivo com erros)
java -jar dist/java-sort-analyzer.jar --gerar-dados dados-exemplo

# Executar bateria de benchmark headless (1k a 100k) e salvar CSV
java -jar dist/java-sort-analyzer.jar --benchmark resultados/benchmark.csv

# Executar bateria completa com metodologia uniforme (3 repetições em todos os testes)
java -jar dist/java-sort-analyzer.jar --benchmark resultados/benchmark.csv --uniforme

# Executar bateria rápida (1k, 5k e 10k)
java -jar dist/java-sort-analyzer.jar --benchmark resultados/benchmark_rapido.csv --rapido
```

---

## Empacotamento Nativo (`jpackage` — Java embutido)

Para distribuir a aplicação sem exigir que o usuário final tenha o Java instalado:

- **Windows:** execute `scripts\package-windows.bat` em um ambiente Windows com JDK 17+ (gera `JavaSortAnalyzer.exe` portátil com runtime embutido).
- **Linux:** execute `./scripts/package-linux.sh`.
- **CI/CD:** o workflow em [`.github/workflows/build-release.yml`](.github/workflows/build-release.yml) compila os testes, o `.jar` e o pacote portátil Windows automaticamente no GitHub Actions.

---

## Documentação Adicional

- [Arquitetura e Metodologia Técnica](docs/ARCHITECTURE.md)
- Arquivos didáticos prontos para importação em [`dados-exemplo/`](dados-exemplo/)

## Licença

Distribuído sob a licença [MIT](LICENSE).
