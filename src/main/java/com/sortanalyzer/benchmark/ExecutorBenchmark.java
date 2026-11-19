package com.sortanalyzer.benchmark;

import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.sort.AlgoritmoOrdenacao;
import com.sortanalyzer.sort.VerificadorOrdenacao;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Executa a medição isolada de desempenho de um algoritmo de ordenação.
 * <p>
 * Garante rigor metodológico: a clonagem do vetor original e a autoverificação
 * ocorrem FORA do intervalo cronometrado por {@link System#nanoTime()}, medindo
 * exclusivamente o processo de ordenação.
 */
public final class ExecutorBenchmark {

    /**
     * Retorno composto contendo a métrica de desempenho e o vetor resultante já ordenado.
     */
    public record ExecucaoIndividual(ResultadoOrdenacao resultado, ImagemSatelite[] dadosOrdenados) {
    }

    private ExecutorBenchmark() {
    }

    /**
     * Executa um algoritmo sobre uma cópia exata de {@code dadosOriginais}
     * e retorna tanto as métricas quanto o vetor ordenado para exibição "Depois".
     */
    public static ExecucaoIndividual executarUmaVez(AlgoritmoOrdenacao algoritmo,
                                                    ImagemSatelite[] dadosOriginais,
                                                    TipoCenario cenario,
                                                    CriterioOrdenacao criterio) {
        Comparator<ImagemSatelite> comparador = criterio.getComparador();

        // Clonagem FORA da medição de tempo
        ImagemSatelite[] copia = Arrays.copyOf(dadosOriginais, dadosOriginais.length);

        long inicio = System.nanoTime();
        algoritmo.ordenar(copia, comparador);
        long duracaoNanos = System.nanoTime() - inicio;

        // Autoverificação FORA da medição de tempo
        boolean valido = VerificadorOrdenacao.estaOrdenado(copia, comparador)
                && VerificadorOrdenacao.possuiMesmosElementos(dadosOriginais, copia);

        ResultadoOrdenacao resultado = new ResultadoOrdenacao(
                algoritmo.getNome(),
                algoritmo.getComplexidadeMedia(),
                algoritmo.getComplexidadePiorCaso(),
                algoritmo.isEstavel(),
                dadosOriginais.length,
                cenario,
                criterio,
                duracaoNanos,
                1,
                valido
        );

        return new ExecucaoIndividual(resultado, copia);
    }

    /**
     * Executa {@code repeticoes} medições independentes sobre cópias idênticas da mesma base
     * e calcula a média aritmética do tempo exclusivo de ordenação.
     */
    public static ResultadoOrdenacao executarMedia(AlgoritmoOrdenacao algoritmo,
                                                   ImagemSatelite[] dadosOriginais,
                                                   TipoCenario cenario,
                                                   CriterioOrdenacao criterio,
                                                   int repeticoes) {
        int reps = Math.max(1, repeticoes);
        Comparator<ImagemSatelite> comparador = criterio.getComparador();

        long somaNanos = 0L;
        boolean todasValidas = true;

        for (int r = 0; r < reps; r++) {
            ImagemSatelite[] copia = Arrays.copyOf(dadosOriginais, dadosOriginais.length);

            long inicio = System.nanoTime();
            algoritmo.ordenar(copia, comparador);
            long duracao = System.nanoTime() - inicio;

            somaNanos += duracao;
            if (!VerificadorOrdenacao.estaOrdenado(copia, comparador)) {
                todasValidas = false;
            }
            // Valida integridade completa na primeira repetição
            if (r == 0 && !VerificadorOrdenacao.possuiMesmosElementos(dadosOriginais, copia)) {
                todasValidas = false;
            }
        }

        long mediaNanos = somaNanos / reps;

        return new ResultadoOrdenacao(
                algoritmo.getNome(),
                algoritmo.getComplexidadeMedia(),
                algoritmo.getComplexidadePiorCaso(),
                algoritmo.isEstavel(),
                dadosOriginais.length,
                cenario,
                criterio,
                mediaNanos,
                reps,
                todasValidas
        );
    }
}
