package com.sortanalyzer.visual;

/**
 * Representa uma operação atômica gravada para reprodução animada
 * (avançar, pausar, passo-a-passo para frente/trás e modo corrida).
 */
public record PassoOrdenacao(
        TipoPasso tipo,
        int indiceA,
        int indiceB,
        int valorNovo,
        int valorAnterior,
        String descricao
) {

    public enum TipoPasso {
        COMPARAR,
        TROCAR,
        ATRIBUIR,
        DEFINIR_PIVO,
        MARCAR_ORDENADO,
        CONCLUIDO
    }

    public static PassoOrdenacao comparar(int a, int b, String descricao) {
        return new PassoOrdenacao(TipoPasso.COMPARAR, a, b, 0, 0, descricao);
    }

    public static PassoOrdenacao trocar(int a, int b, String descricao) {
        return new PassoOrdenacao(TipoPasso.TROCAR, a, b, 0, 0, descricao);
    }

    public static PassoOrdenacao atribuir(int indice, int valorNovo, int valorAnterior, String descricao) {
        return new PassoOrdenacao(TipoPasso.ATRIBUIR, indice, -1, valorNovo, valorAnterior, descricao);
    }

    public static PassoOrdenacao definirPivo(int indice, String descricao) {
        return new PassoOrdenacao(TipoPasso.DEFINIR_PIVO, indice, -1, 0, 0, descricao);
    }

    public static PassoOrdenacao marcarOrdenado(int indice, String descricao) {
        return new PassoOrdenacao(TipoPasso.MARCAR_ORDENADO, indice, -1, 0, 0, descricao);
    }

    public static PassoOrdenacao concluido(String descricao) {
        return new PassoOrdenacao(TipoPasso.CONCLUIDO, -1, -1, 0, 0, descricao);
    }
}
