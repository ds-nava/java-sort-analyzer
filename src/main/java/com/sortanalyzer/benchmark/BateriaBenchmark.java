package com.sortanalyzer.benchmark;

import com.sortanalyzer.io.GeradorDados;
import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.sort.AlgoritmoOrdenacao;
import com.sortanalyzer.sort.OrdenacaoCanceladaException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Executa a matriz completa de benchmarks (Tamanhos × Cenários × Algoritmos),
 * garantindo que todos os algoritmos testem exatamente a mesma base determinística
 * em cada célula da matriz.
 */
public final class BateriaBenchmark {

    @FunctionalInterface
    public interface OuvinteProgresso {
        void aoAtualizar(int concluidos, int total, String mensagem, ResultadoOrdenacao novoResultado);
    }

    private BateriaBenchmark() {
    }

    public static List<ResultadoOrdenacao> executar(ConfiguracaoBateria config,
                                                    OuvinteProgresso ouvinte,
                                                    BooleanSupplier cancelado) {
        List<ResultadoOrdenacao> resultados = new ArrayList<>(config.totalCombinacoes());
        int total = config.totalCombinacoes();
        int concluidos = 0;

        if (config.isExecutarWarmUp()) {
            notificar(ouvinte, 0, total, "Aquecendo a JVM (warm-up JIT)...", null);
            executarWarmUp(config, cancelado);
        }

        for (int tamanho : config.getTamanhos()) {
            for (TipoCenario cenario : config.getCenarios()) {
                if (estaCancelado(cancelado)) {
                    throw new OrdenacaoCanceladaException();
                }

                // Gera UMA única base para o par (tamanho, cenário) — todos os algoritmos ordenam cópias idênticas dela
                ImagemSatelite[] baseComum = GeradorDados.gerar(
                        tamanho, cenario, config.getCriterio(), config.getSeed());

                for (AlgoritmoOrdenacao algoritmo : config.getAlgoritmos()) {
                    if (estaCancelado(cancelado)) {
                        throw new OrdenacaoCanceladaException();
                    }

                    int reps = config.calcularRepeticoesPara(algoritmo, tamanho);
                    notificar(ouvinte, concluidos, total,
                            String.format("Executando %s | n=%,d | %s (%d rep.)...",
                                    algoritmo.getNome(), tamanho, cenario.getRotulo(), reps),
                            null);

                    ResultadoOrdenacao res = ExecutorBenchmark.executarMedia(
                            algoritmo, baseComum, cenario, config.getCriterio(), reps);
                    resultados.add(res);
                    concluidos++;

                    notificar(ouvinte, concluidos, total,
                            String.format("Concluído %s | n=%,d | %s -> %s",
                                    algoritmo.getNome(), tamanho, cenario.getRotulo(), res.getTempoFormatado()),
                            res);
                }
            }
        }

        return resultados;
    }

    private static void executarWarmUp(ConfiguracaoBateria config, BooleanSupplier cancelado) {
        ImagemSatelite[] amostra = GeradorDados.gerar(
                1_000, TipoCenario.ALEATORIO, config.getCriterio(), config.getSeed());
        for (AlgoritmoOrdenacao alg : config.getAlgoritmos()) {
            if (estaCancelado(cancelado)) {
                throw new OrdenacaoCanceladaException();
            }
            ExecutorBenchmark.executarMedia(alg, amostra, TipoCenario.ALEATORIO, config.getCriterio(), 2);
        }
    }

    private static boolean estaCancelado(BooleanSupplier cancelado) {
        return (cancelado != null && cancelado.getAsBoolean()) || Thread.currentThread().isInterrupted();
    }

    private static void notificar(OuvinteProgresso ouvinte,
                                  int concluidos,
                                  int total,
                                  String mensagem,
                                  ResultadoOrdenacao resultado) {
        if (ouvinte != null) {
            ouvinte.aoAtualizar(concluidos, total, mensagem, resultado);
        }
    }
}
