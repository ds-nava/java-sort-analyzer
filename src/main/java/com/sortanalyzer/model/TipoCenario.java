package com.sortanalyzer.model;

/**
 * Representa a disposição inicial dos dados antes da execução dos algoritmos de ordenação.
 */
public enum TipoCenario {

    ALEATORIO("Aleatório", "Elementos distribuídos sem ordem definida (caso médio)."),
    ORDENADO("Já Ordenado", "Elementos em ordem crescente (testa melhor caso e estabilidade do pivô)."),
    INVERSO("Inversamente Ordenado", "Elementos em ordem decrescente (pior caso para algoritmos quadráticos)."),
    SEMI_ORDENADO("Semi-Ordenado (90%)", "90% ordenado com 10% de perturbações (simula lotes incrementais de satélite)."),
    EXTERNO("Arquivo Externo", "Dados carregados a partir de arquivo .csv ou .txt externo."),
    MANUAL("Entrada Manual", "Valores digitados diretamente pelo usuário.");

    private final String rotulo;
    private final String descricao;

    TipoCenario(String rotulo, String descricao) {
        this.rotulo = rotulo;
        this.descricao = descricao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna apenas os 4 cenários usados na geração automática e na bateria de benchmarks.
     */
    public static TipoCenario[] cenariosGeracao() {
        return new TipoCenario[]{ALEATORIO, ORDENADO, INVERSO, SEMI_ORDENADO};
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
