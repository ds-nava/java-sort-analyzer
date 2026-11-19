package com.sortanalyzer.model;

import java.util.Locale;

/**
 * Armazena as métricas coletadas após a execução de um algoritmo de ordenação.
 */
public final class ResultadoOrdenacao {

    private final String nomeAlgoritmo;
    private final String complexidadeMedia;
    private final String complexidadePiorCaso;
    private final boolean estavel;
    private final int tamanhoEntrada;
    private final TipoCenario cenario;
    private final CriterioOrdenacao criterio;
    private final long tempoNanos;
    private final int repeticoes;
    private final boolean ordenadoCorretamente;

    public ResultadoOrdenacao(String nomeAlgoritmo,
                              String complexidadeMedia,
                              String complexidadePiorCaso,
                              boolean estavel,
                              int tamanhoEntrada,
                              TipoCenario cenario,
                              CriterioOrdenacao criterio,
                              long tempoNanos,
                              int repeticoes,
                              boolean ordenadoCorretamente) {
        this.nomeAlgoritmo = nomeAlgoritmo;
        this.complexidadeMedia = complexidadeMedia;
        this.complexidadePiorCaso = complexidadePiorCaso;
        this.estavel = estavel;
        this.tamanhoEntrada = tamanhoEntrada;
        this.cenario = cenario;
        this.criterio = criterio;
        this.tempoNanos = tempoNanos;
        this.repeticoes = Math.max(1, repeticoes);
        this.ordenadoCorretamente = ordenadoCorretamente;
    }

    public String getNomeAlgoritmo() {
        return nomeAlgoritmo;
    }

    public String getComplexidadeMedia() {
        return complexidadeMedia;
    }

    public String getComplexidadePiorCaso() {
        return complexidadePiorCaso;
    }

    public boolean isEstavel() {
        return estavel;
    }

    public int getTamanhoEntrada() {
        return tamanhoEntrada;
    }

    public TipoCenario getCenario() {
        return cenario;
    }

    public CriterioOrdenacao getCriterio() {
        return criterio;
    }

    public long getTempoNanos() {
        return tempoNanos;
    }

    public double getTempoMilissegundos() {
        return tempoNanos / 1_000_000.0;
    }

    public int getRepeticoes() {
        return repeticoes;
    }

    public boolean isOrdenadoCorretamente() {
        return ordenadoCorretamente;
    }

    public String getTempoFormatado() {
        double ms = getTempoMilissegundos();
        if (ms >= 1000.0) {
            return String.format(Locale.US, "%.2f ms (%.2f s)", ms, ms / 1000.0);
        }
        return String.format(Locale.US, "%.3f ms", ms);
    }

    public String paraLinhaCsv() {
        return String.format(Locale.US, "%s;%s;%s;%s;%d;%s;%s;%.4f;%d;%s",
                nomeAlgoritmo,
                complexidadeMedia,
                complexidadePiorCaso,
                estavel ? "Sim" : "Não",
                tamanhoEntrada,
                cenario.getRotulo(),
                criterio.getRotulo(),
                getTempoMilissegundos(),
                repeticoes,
                ordenadoCorretamente ? "OK" : "FALHA");
    }
}
