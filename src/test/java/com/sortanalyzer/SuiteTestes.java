package com.sortanalyzer;

import com.sortanalyzer.benchmark.BateriaBenchmark;
import com.sortanalyzer.benchmark.ConfiguracaoBateria;
import com.sortanalyzer.benchmark.ExecutorBenchmark;
import com.sortanalyzer.io.EscritorArquivoCatalogo;
import com.sortanalyzer.io.GeradorDados;
import com.sortanalyzer.io.LeitorArquivoCatalogo;
import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.RelatorioLeitura;
import com.sortanalyzer.model.RelatorioLeitura.FormatoDetectado;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.sort.AlgoritmoOrdenacao;
import com.sortanalyzer.sort.BubbleSort;
import com.sortanalyzer.sort.HeapSort;
import com.sortanalyzer.sort.InsertionSort;
import com.sortanalyzer.sort.MergeSort;
import com.sortanalyzer.sort.QuickSort;
import com.sortanalyzer.sort.RegistroAlgoritmos;
import com.sortanalyzer.sort.VerificadorOrdenacao;
import com.sortanalyzer.ui.PainelGraficoComparativo;
import com.sortanalyzer.visual.GravadorPassosAnimacao.TipoAlgoritmoVisual;
import com.sortanalyzer.visual.ReprodutorAnimacao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Suíte completa de testes automatizados (sem dependências externas, executável em qualquer JDK 17+).
 * Valida os 5 algoritmos de ordenação, estabilidade, escala de 100.000 itens, leitura flexível,
 * tratamento de erros, benchmarks, gravação/retrocesso das animações e exportação PNG.
 */
public final class SuiteTestes {

    private static int testesExecutados = 0;
    private static int testesFalhos = 0;

    public static void main(String[] args) {
        executar("Algoritmos em casos de borda (vazio, 1 item, 2 itens, todos iguais)", SuiteTestes::testarCasosDeBorda);
        executar("Todos os 5 algoritmos em 4 cenários x 3 critérios", SuiteTestes::testarMatrizAlgoritmos);
        executar("Estabilidade de Bubble, Insertion e Merge Sort", SuiteTestes::testarEstabilidade);
        executar("Escala de 100.000 itens ordenados e inversos (sem StackOverflow)", SuiteTestes::testarEscala100k);
        executar("Determinismo do GeradorDados e Entrada Manual", SuiteTestes::testarGeradorEManual);
        executar("Leitura flexível (CSV completo, lista numérica, erros parciais e arquivo vazio)", SuiteTestes::testarLeituraFlexivel);
        executar("Benchmark com mesma base e repetições inteligentes", SuiteTestes::testarBenchmark);
        executar("Animação: avanço até o fim e retrocesso exato (todos os 5 algoritmos)", SuiteTestes::testarAnimacaoAvancoERetrocesso);
        executar("Exportação de gráfico Java 2D em PNG (headless)", SuiteTestes::testarExportacaoGraficoPng);

        System.out.println("\n==========================================");
        System.out.printf("RESULTADO DOS TESTES: %d executados | %d falhas%n", testesExecutados, testesFalhos);
        System.out.println("==========================================");

        if (testesFalhos > 0) {
            System.exit(1);
        }
    }

    private static void executar(String nome, TesteRunnable teste) {
        testesExecutados++;
        try {
            teste.run();
            System.out.println("[OK]    " + nome);
        } catch (Throwable ex) {
            testesFalhos++;
            System.err.println("[FALHA] " + nome + " -> " + ex);
            ex.printStackTrace(System.err);
        }
    }

    private static void testarCasosDeBorda() {
        Comparator<ImagemSatelite> comp = CriterioOrdenacao.ID.getComparador();
        ImagemSatelite unico = new ImagemSatelite(10, "Amazônia-1", 1000L, "AM", 5.0);
        ImagemSatelite[] iguais = new ImagemSatelite[500];
        Arrays.fill(iguais, unico);

        for (AlgoritmoOrdenacao alg : RegistroAlgoritmos.todos()) {
            ImagemSatelite[] vazio = new ImagemSatelite[0];
            alg.ordenar(vazio, comp);
            garantir(vazio.length == 0, alg.getNome() + " falhou em vetor vazio");

            ImagemSatelite[] um = new ImagemSatelite[]{unico};
            alg.ordenar(um, comp);
            garantir(um[0].equals(unico), alg.getNome() + " falhou em 1 elemento");

            ImagemSatelite[] dois = new ImagemSatelite[]{
                    new ImagemSatelite(50, "Sat", 1000L, "PA", 2.0),
                    new ImagemSatelite(10, "Sat", 1000L, "PA", 1.0)
            };
            alg.ordenar(dois, comp);
            garantir(dois[0].getId() == 10 && dois[1].getId() == 50, alg.getNome() + " falhou em 2 elementos");

            ImagemSatelite[] copiaIguais = Arrays.copyOf(iguais, iguais.length);
            alg.ordenar(copiaIguais, comp);
            garantir(VerificadorOrdenacao.estaOrdenado(copiaIguais, comp), alg.getNome() + " falhou em elementos iguais");
        }
    }

    private static void testarMatrizAlgoritmos() {
        for (CriterioOrdenacao criterio : CriterioOrdenacao.values()) {
            for (TipoCenario cenario : TipoCenario.cenariosGeracao()) {
                ImagemSatelite[] base = GeradorDados.gerar(300, cenario, criterio, 99L);
                for (AlgoritmoOrdenacao alg : RegistroAlgoritmos.todos()) {
                    ImagemSatelite[] copia = Arrays.copyOf(base, base.length);
                    alg.ordenar(copia, criterio.getComparador());
                    garantir(VerificadorOrdenacao.estaOrdenado(copia, criterio.getComparador()),
                            alg.getNome() + " não ordenou em " + cenario + "/" + criterio);
                    garantir(VerificadorOrdenacao.possuiMesmosElementos(base, copia),
                            alg.getNome() + " alterou elementos em " + cenario + "/" + criterio);
                }
            }
        }
    }

    private static void testarEstabilidade() {
        ImagemSatelite[] base = {
                new ImagemSatelite(10, "Sat-A", 100L, "AM", 1.0),
                new ImagemSatelite(5, "Sat-B", 200L, "PA", 2.0),
                new ImagemSatelite(10, "Sat-C", 300L, "MT", 3.0),
                new ImagemSatelite(5, "Sat-D", 400L, "RO", 4.0),
                new ImagemSatelite(10, "Sat-E", 500L, "AC", 5.0)
        };

        List<AlgoritmoOrdenacao> estaveis = List.of(new BubbleSort(), new InsertionSort(), new MergeSort());
        for (AlgoritmoOrdenacao alg : estaveis) {
            ImagemSatelite[] copia = Arrays.copyOf(base, base.length);
            alg.ordenar(copia, CriterioOrdenacao.ID.getComparador());
            garantir(copia[0].getSatelite().equals("Sat-B") && copia[1].getSatelite().equals("Sat-D"),
                    alg.getNome() + " violou estabilidade para ID=5");
            garantir(copia[2].getSatelite().equals("Sat-A")
                            && copia[3].getSatelite().equals("Sat-C")
                            && copia[4].getSatelite().equals("Sat-E"),
                    alg.getNome() + " violou estabilidade para ID=10");
        }
    }

    private static void testarEscala100k() {
        List<AlgoritmoOrdenacao> rapidos = List.of(new MergeSort(), new QuickSort(), new HeapSort());
        for (TipoCenario cen : List.of(TipoCenario.ALEATORIO, TipoCenario.ORDENADO, TipoCenario.INVERSO)) {
            ImagemSatelite[] base = GeradorDados.gerar(100_000, cen, CriterioOrdenacao.ID, 42L);
            for (AlgoritmoOrdenacao alg : rapidos) {
                ImagemSatelite[] copia = Arrays.copyOf(base, base.length);
                alg.ordenar(copia, CriterioOrdenacao.ID.getComparador());
                garantir(VerificadorOrdenacao.estaOrdenado(copia, CriterioOrdenacao.ID.getComparador()),
                        alg.getNome() + " falhou em 100.000 itens (" + cen + ")");
            }
        }
    }

    private static void testarGeradorEManual() {
        ImagemSatelite[] a = GeradorDados.gerar(500, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, 42L);
        ImagemSatelite[] b = GeradorDados.gerar(500, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, 42L);
        garantir(Arrays.equals(a, b), "GeradorDados não foi determinístico com mesma seed");

        ImagemSatelite[] manual = GeradorDados.converterEntradaManual("45, 12; 99 7\n30");
        garantir(manual.length == 5 && manual[0].getId() == 45 && manual[4].getId() == 30,
                "Parser manual falhou em inteiros");

        boolean lancouErro = false;
        try {
            GeradorDados.converterEntradaManual("10, abc, 20");
        } catch (IllegalArgumentException ex) {
            lancouErro = true;
        }
        garantir(lancouErro, "Parser manual deveria rejeitar valor não numérico");
    }

    private static void testarLeituraFlexivel() throws IOException {
        Path tempDir = Files.createTempDirectory("aps-testes-");
        try {
            EscritorArquivoCatalogo.gerarArquivosExemplo(tempDir, false);

            RelatorioLeitura relCsv = LeitorArquivoCatalogo.ler(tempDir.resolve("catalogo_1000.csv"));
            garantir(relCsv.getFormatoDetectado() == FormatoDetectado.CATALOGO_COMPLETO, "Formato CSV não detectado");
            garantir(relCsv.getRegistros().length == 1_000 && !relCsv.possuiAvisos(), "CSV 1000 deveria carregar 1000 sem avisos");

            RelatorioLeitura relTxt = LeitorArquivoCatalogo.ler(tempDir.resolve("numeros_simples.txt"));
            garantir(relTxt.getFormatoDetectado() == FormatoDetectado.LISTA_NUMERICA, "Formato numérico não detectado");
            garantir(relTxt.getRegistros().length == 200, "Lista numérica deveria ter 200 registros");

            RelatorioLeitura relErros = LeitorArquivoCatalogo.ler(tempDir.resolve("arquivo_com_erros.csv"));
            garantir(relErros.getRegistros().length == 4, "Arquivo com erros deveria carregar 4 linhas válidas");
            garantir(relErros.getAvisos().size() == 3, "Arquivo com erros deveria reportar 3 avisos");

            Path vazio = tempDir.resolve("vazio.txt");
            Files.writeString(vazio, "   \n# comentario\n");
            boolean falhouVazio = false;
            try {
                LeitorArquivoCatalogo.ler(vazio);
            } catch (IOException ex) {
                falhouVazio = true;
            }
            garantir(falhouVazio, "Arquivo vazio deveria lançar IOException");
        } finally {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    private static void testarBenchmark() {
        ConfiguracaoBateria cfg = new ConfiguracaoBateria(
                List.of(200, 50_000),
                List.of(TipoCenario.ORDENADO),
                RegistroAlgoritmos.todos(),
                CriterioOrdenacao.ID,
                42L,
                3,
                true,
                false
        );
        garantir(cfg.calcularRepeticoesPara(new BubbleSort(), 50_000) == 1,
                "Repetições inteligentes deveria usar 1 rep para Bubble em 50k");
        garantir(cfg.calcularRepeticoesPara(new QuickSort(), 50_000) == 3,
                "Repetições inteligentes deveria manter 3 reps para Quick em 50k");

        ConfiguracaoBateria pequena = new ConfiguracaoBateria(
                List.of(300),
                List.of(TipoCenario.ALEATORIO, TipoCenario.INVERSO),
                RegistroAlgoritmos.todos(),
                CriterioOrdenacao.ID,
                42L,
                2,
                true,
                false
        );
        List<ResultadoOrdenacao> res = BateriaBenchmark.executar(pequena, null, () -> false);
        garantir(res.size() == 10, "Esperados 10 resultados na bateria pequena");
        for (ResultadoOrdenacao r : res) {
            garantir(r.isOrdenadoCorretamente(), "Falha na verificação do resultado de " + r.getNomeAlgoritmo());
            garantir(r.getTempoNanos() >= 0, "Tempo negativo inválido");
        }
    }

    private static void testarAnimacaoAvancoERetrocesso() {
        int[] base = GeradorDados.gerarSequenciaVisual(35, TipoCenario.ALEATORIO, 77L);
        int[] esperadoOrdenado = Arrays.copyOf(base, base.length);
        Arrays.sort(esperadoOrdenado);

        for (TipoAlgoritmoVisual tipo : TipoAlgoritmoVisual.values()) {
            ReprodutorAnimacao rep = new ReprodutorAnimacao(tipo, base);
            while (rep.avancarUmPasso()) {
                // percorre todos os passos
            }
            garantir(rep.isConcluido(), "Animação de " + tipo + " não marcou conclusão");
            garantir(Arrays.equals(rep.getValoresAtuais(), esperadoOrdenado),
                    "Animação de " + tipo + " não resultou no vetor ordenado esperado");

            while (rep.voltarUmPasso()) {
                // retrocede até o início
            }
            garantir(rep.getIndicePasso() == 0 && !rep.isConcluido(),
                    "Retrocesso de " + tipo + " não retornou ao passo 0");
            garantir(Arrays.equals(rep.getValoresAtuais(), base),
                    "Retrocesso de " + tipo + " não restaurou a sequência inicial exata");
            garantir(rep.getTotalComparacoes() == 0 && rep.getTotalMovimentacoes() == 0,
                    "Contadores de " + tipo + " não zeraram no retrocesso completo");
        }
    }

    private static void testarExportacaoGraficoPng() throws IOException {
        ImagemSatelite[] base = GeradorDados.gerar(200, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, 42L);
        List<ResultadoOrdenacao> resultados = RegistroAlgoritmos.todos().stream()
                .map(alg -> ExecutorBenchmark.executarUmaVez(alg, base, TipoCenario.ALEATORIO, CriterioOrdenacao.ID).resultado())
                .toList();

        PainelGraficoComparativo grafico = new PainelGraficoComparativo();
        grafico.setResultados(resultados, "Teste Headless PNG");
        Path png = Files.createTempFile("grafico-aps-", ".png");
        try {
            grafico.exportarPng(png, 800, 400);
            garantir(Files.exists(png) && Files.size(png) > 1_000, "PNG gerado está vazio ou inválido");
        } finally {
            Files.deleteIfExists(png);
        }
    }

    private static void garantir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    @FunctionalInterface
    private interface TesteRunnable {
        void run() throws Exception;
    }
}
