# Arquitetura e Decisões Técnicas — Java Sort Analyzer

Este documento descreve a arquitetura de pacotes, as escolhas algorítmicas e a metodologia de medição adotadas no **Java Sort Analyzer**.

---

## 1. Estrutura de Pacotes

```text
com.sortanalyzer
├── Main                 # Ponto de entrada (GUI Swing + modo CLI headless)
├── model/               # Entidades de domínio e DTOs imutáveis
│   ├── ImagemSatelite
│   ├── CriterioOrdenacao
│   ├── TipoCenario
│   ├── ResultadoOrdenacao
│   └── RelatorioLeitura
├── sort/                # Implementações puras dos 5 algoritmos + autoverificação
│   ├── AlgoritmoOrdenacao
│   ├── BubbleSort
│   ├── InsertionSort
│   ├── MergeSort
│   ├── QuickSort
│   ├── HeapSort
│   ├── RegistroAlgoritmos
│   ├── VerificadorOrdenacao
│   └── OrdenacaoCanceladaException
├── io/                  # Geração determinística, leitura flexível e exportação CSV/TXT
│   ├── GeradorDados
│   ├── LeitorArquivoCatalogo
│   └── EscritorArquivoCatalogo
├── benchmark/           # Medição isolada e matriz automatizada de benchmarks
│   ├── ExecutorBenchmark
│   ├── ConfiguracaoBateria
│   └── BateriaBenchmark
├── visual/              # Motor animado desacoplado (Individual e Corrida Simultânea)
│   ├── PassoOrdenacao
│   ├── GravadorPassosAnimacao
│   ├── ReprodutorAnimacao
│   ├── PainelBarrasCanvas
│   ├── PainelModoIndividual
│   └── PainelModoCorrida
└── ui/                  # Interface desktop Swing + Java 2D (Graphics2D)
    ├── JanelaPrincipal
    ├── AbaAnaliseCatalogo
    ├── AbaVisualizadorAnimado
    ├── AbaBateriaBenchmark
    ├── PainelGraficoComparativo
    ├── PainelTabelaPaginada
    ├── TabelaCatalogoModel
    └── TemaVisual
```

---

## 2. Decisões Algorítmicas

O conjunto combina técnicas clássicas de complexidade quadrática $O(n^2)$ e log-linear $O(n \log n)$ para evidenciar diferenças de escalabilidade, estabilidade e uso de memória:

1. **`BubbleSort` (Otimizado com parada antecipada):** interrompe a execução se nenhuma troca ocorrer em uma varredura completa, atingindo $O(n)$ em vetores já ordenados.
2. **`InsertionSort` (Deslocamento):** realiza deslocamento de chave sem trocas sucessivas, sendo estável e eficiente em entradas pequenas ou semi-ordenadas.
3. **`MergeSort` (Buffer auxiliar único):** aloca um único vetor auxiliar $O(n)$ no início da chamada, evita intercalação desnecessária quando `dados[meio] <= dados[meio + 1]` e preserva estabilidade.
4. **`QuickSort` (Mediana de Três + Partição de Hoare + Eliminação de Recursão de Cauda):**
   - A mediana de três evita degradação para $O(n^2)$ em dados ordenados ou inversos.
   - O particionamento de Hoare lida de forma balanceada com chaves duplicadas.
   - A recursão ocorre sempre na menor partição (iterando na maior), limitando a pilha a $O(\log n)$ e eliminando o risco de `StackOverflowError` em 100.000+ elementos.
5. **`HeapSort` (Iterativo *in-place*):** constrói o Max-Heap *bottom-up* em $O(n)$ e utiliza `descerNoHeap` iterativo, garantindo $O(n \log n)$ em todos os cenários com $O(1)$ de memória auxiliar.

---

## 3. Metodologia de Benchmark (Isolamento da Medição)

- **Mesma base para todos os algoritmos:** em cada teste, o sistema gera ou carrega uma única base original (com `seed` fixa no gerador determinístico). Cada algoritmo recebe uma cópia idêntica via `Arrays.copyOf` **antes** do início do cronômetro.
- **Cronômetro estritamente isolado:** `System.nanoTime()` envolve **exclusivamente** a invocação `algoritmo.ordenar(copia, comparador)`, excluindo leitura de disco, geração de registros, clonagem, autoverificação e renderização.
- **Autoverificação pós-ordenação:** `VerificadorOrdenacao` valida tanto a ordenação monotônica quanto a integridade do multiconjunto (nenhum registro perdido ou duplicado).
- **Warm-up da JVM e Repetições Inteligentes:** na bateria em lote, uma etapa opcional de aquecimento reduz o impacto inicial do compilador JIT; em entradas grandes ($\ge 25.000$), algoritmos $O(n^2)$ podem usar 1 repetição cronometrada (configurável) enquanto $O(n \log n)$ mantém a média de múltiplas rodadas.

---

## 4. Desacoplamento do Visualizador Animado

Para nunca contaminar o motor de benchmark com `Thread.sleep()` ou chamadas de interface, a visualização utiliza uma arquitetura baseada em **gravação de passos atômicos**:

- `GravadorPassosAnimacao` executa versões instrumentadas sobre uma amostra visual (10 a 150 barras) e produz uma lista imutável de `PassoOrdenacao` (`COMPARAR`, `TROCAR`, `ATRIBUIR`, `DEFINIR_PIVO`, `MARCAR_ORDENADO`, `CONCLUIDO`).
- `ReprodutorAnimacao` aplica e desfaz passos em $O(1)$, viabilizando **Play/Pause**, **Passo Anterior/Próximo**, **Reinício**, ajuste dinâmico de velocidade e o **Modo Corrida Simultânea** (os 5 algoritmos avançando sincronizados sobre a mesma sequência inicial).

---

## 5. Leitura Flexível de Arquivos Externos

`LeitorArquivoCatalogo` detecta automaticamente dois formatos em arquivos `.csv` ou `.txt` (com remoção automática de BOM UTF-8):

1. **Catálogo Completo (`;`):** `id;satelite;data_captura;regiao;indice_desmatamento`
2. **Lista Numérica Simples:** números separados por quebra de linha, vírgula, ponto-e-vírgula ou espaço.

Linhas corrompidas ou malformadas são isoladas e reportadas em `RelatorioLeitura`, permitindo o carregamento seguro dos registros válidos sem interromper a aplicação.
