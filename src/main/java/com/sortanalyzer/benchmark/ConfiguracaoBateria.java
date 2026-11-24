package com.sortanalyzer.benchmark;

import com.sortanalyzer.io.GeradorDados;
import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.sort.AlgoritmoOrdenacao;
import com.sortanalyzer.sort.RegistroAlgoritmos;

import java.util.Arrays;
import java.util.List;

/**
 * Parâmetros configuráveis para execução da Bateria de Benchmark em lote.
 */
public final class ConfiguracaoBateria {

    public static final int LIMIAR_QUADRATICO_REPETICAO_UNICA = 25_000;

    private final List<Integer> tamanhos;
    private final List<TipoCenario> cenarios;
    private final List<AlgoritmoOrdenacao> algoritmos;
    private final CriterioOrdenacao criterio;
    private final long seed;
    private final int repeticoes;
    private final boolean repeticoesInteligentes;
    private final boolean executarWarmUp;

    public ConfiguracaoBateria(List<Integer> tamanhos,
                               List<TipoCenario> cenarios,
                               List<AlgoritmoOrdenacao> algoritmos,
                               CriterioOrdenacao criterio,
                               long seed,
                               int repeticoes,
                               boolean repeticoesInteligentes,
                               boolean executarWarmUp) {
        if (tamanhos == null || tamanhos.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um tamanho de entrada.");
        }
        if (cenarios == null || cenarios.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um cenário.");
        }
        if (algoritmos == null || algoritmos.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um algoritmo.");
        }
        this.tamanhos = List.copyOf(tamanhos);
        this.cenarios = List.copyOf(cenarios);
        this.algoritmos = List.copyOf(algoritmos);
        this.criterio = criterio != null ? criterio : CriterioOrdenacao.ID;
        this.seed = seed;
        this.repeticoes = Math.max(1, repeticoes);
        this.repeticoesInteligentes = repeticoesInteligentes;
        this.executarWarmUp = executarWarmUp;
    }

    public static ConfiguracaoBateria padraoCompleto() {
        return new ConfiguracaoBateria(
                List.of(1_000, 10_000, 50_000, 100_000),
                Arrays.asList(TipoCenario.cenariosGeracao()),
                RegistroAlgoritmos.todos(),
                CriterioOrdenacao.ID,
                GeradorDados.SEED_PADRAO,
                3,
                true,
                true
        );
    }

    public static ConfiguracaoBateria padraoCompletoUniforme() {
        return new ConfiguracaoBateria(
                List.of(1_000, 10_000, 50_000, 100_000),
                Arrays.asList(TipoCenario.cenariosGeracao()),
                RegistroAlgoritmos.todos(),
                CriterioOrdenacao.ID,
                GeradorDados.SEED_PADRAO,
                3,
                false,
                true
        );
    }

    public static ConfiguracaoBateria rapida() {
        return new ConfiguracaoBateria(
                List.of(1_000, 5_000, 10_000),
                Arrays.asList(TipoCenario.cenariosGeracao()),
                RegistroAlgoritmos.todos(),
                CriterioOrdenacao.ID,
                GeradorDados.SEED_PADRAO,
                3,
                true,
                true
        );
    }

    public List<Integer> getTamanhos() {
        return tamanhos;
    }

    public List<TipoCenario> getCenarios() {
        return cenarios;
    }

    public List<AlgoritmoOrdenacao> getAlgoritmos() {
        return algoritmos;
    }

    public CriterioOrdenacao getCriterio() {
        return criterio;
    }

    public long getSeed() {
        return seed;
    }

    public int getRepeticoes() {
        return repeticoes;
    }

    public boolean isRepeticoesInteligentes() {
        return repeticoesInteligentes;
    }

    public boolean isExecutarWarmUp() {
        return executarWarmUp;
    }

    public int calcularRepeticoesPara(AlgoritmoOrdenacao algoritmo, int tamanho) {
        if (repeticoesInteligentes
                && algoritmo.isQuadratico()
                && tamanho >= LIMIAR_QUADRATICO_REPETICAO_UNICA) {
            return 1;
        }
        return repeticoes;
    }

    public int totalCombinacoes() {
        return tamanhos.size() * cenarios.size() * algoritmos.size();
    }
}
