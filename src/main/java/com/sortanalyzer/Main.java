package com.sortanalyzer;

import com.sortanalyzer.benchmark.BateriaBenchmark;
import com.sortanalyzer.benchmark.ConfiguracaoBateria;
import com.sortanalyzer.io.EscritorArquivoCatalogo;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.ui.JanelaPrincipal;
import com.sortanalyzer.ui.TemaVisual;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Ponto de entrada do sistema Amazônia Sort Analyzer.
 * <p>
 * Sem argumentos: inicia a interface gráfica desktop (Java Swing + Java 2D).
 * Com argumentos CLI: permite gerar arquivos de exemplo ou rodar a bateria de benchmark
 * em modo headless (sem interface gráfica).
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            executarCli(args);
            return;
        }

        TemaVisual.aplicarLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            JanelaPrincipal janela = new JanelaPrincipal();
            janela.setVisible(true);
        });
    }

    private static void executarCli(String[] args) throws Exception {
        String comando = args[0].trim().toLowerCase(Locale.ROOT);
        switch (comando) {
            case "--gerar-dados" -> {
                Path destino = Path.of(args.length > 1 ? args[1] : "dados-exemplo");
                System.out.println("Gerando arquivos de exemplo em: " + destino.toAbsolutePath());
                EscritorArquivoCatalogo.gerarArquivosExemplo(destino, true);
                System.out.println("Arquivos gerados com sucesso!");
            }
            case "--benchmark" -> {
                boolean rapido = false;
                boolean uniforme = false;
                String caminhoCsv = "resultados/benchmark.csv";
                for (int i = 1; i < args.length; i++) {
                    if ("--rapido".equalsIgnoreCase(args[i])) {
                        rapido = true;
                    } else if ("--uniforme".equalsIgnoreCase(args[i])) {
                        uniforme = true;
                    } else {
                        caminhoCsv = args[i];
                    }
                }
                ConfiguracaoBateria config = rapido
                        ? ConfiguracaoBateria.rapida()
                        : (uniforme
                                ? ConfiguracaoBateria.padraoCompletoUniforme()
                                : ConfiguracaoBateria.padraoCompleto());

                System.out.println("=== Java Sort Analyzer | Benchmark Headless ===");
                System.out.printf("Java %s | OS: %s (%s) | CPUs: %d%n",
                        System.getProperty("java.version"),
                        System.getProperty("os.name"),
                        System.getProperty("os.arch"),
                        Runtime.getRuntime().availableProcessors());

                List<ResultadoOrdenacao> resultados = BateriaBenchmark.executar(
                        config,
                        (concluidos, total, msg, res) -> {
                            if (res != null) {
                                System.out.printf("[%2d/%2d] %-26s | n=%7d | %-21s | %11.3f ms (%d rep) [%s]%n",
                                        concluidos, total,
                                        res.getNomeAlgoritmo(),
                                        res.getTamanhoEntrada(),
                                        res.getCenario().getRotulo(),
                                        res.getTempoMilissegundos(),
                                        res.getRepeticoes(),
                                        res.isOrdenadoCorretamente() ? "OK" : "FALHA");
                            } else {
                                System.out.println(" -> " + msg);
                            }
                        },
                        () -> false
                );

                Path destino = Path.of(caminhoCsv);
                EscritorArquivoCatalogo.exportarResultadosCsv(destino, resultados);
                System.out.println("CSV salvo em: " + destino.toAbsolutePath());
            }
            case "--ajuda", "-h", "--help" -> imprimirAjuda();
            default -> {
                System.err.println("Argumento desconhecido: " + args[0]);
                imprimirAjuda();
                System.exit(1);
            }
        }
    }

    private static void imprimirAjuda() {
        System.out.println("""
                Java Sort Analyzer — Modos de Uso:
                  java -jar java-sort-analyzer.jar
                      Abre a interface gráfica completa (Swing + Java 2D).

                  java -jar java-sort-analyzer.jar --gerar-dados [pasta]
                      Gera arquivos externos de teste (1k, 10k, 100k, números simples e arquivo com erros).

                  java -jar java-sort-analyzer.jar --benchmark [saida.csv] [--rapido | --uniforme]
                      Executa a bateria de benchmark em linha de comando e exporta CSV.
                      --uniforme força 3 repetições inclusive para O(n²) em 50k e 100k.
                """);
    }
}
