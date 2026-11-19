package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;

/**
 * Contrato comum para todas as implementações puras de algoritmos de ordenação
 * avaliadas no motor de análise de performance e benchmark.
 */
public interface AlgoritmoOrdenacao {

    /**
     * Nome exibido nas tabelas, gráficos e relatórios.
     */
    String getNome();

    /**
     * Notação assintótica para o caso médio.
     */
    String getComplexidadeMedia();

    /**
     * Notação assintótica para o pior caso.
     */
    String getComplexidadePiorCaso();

    /**
     * Notação assintótica para o melhor caso.
     */
    String getComplexidadeMelhorCaso();

    /**
     * Complexidade de memória auxiliar.
     */
    String getComplexidadeEspaco();

    /**
     * Indica se preserva a ordem relativa de elementos com chaves iguais.
     */
    boolean isEstavel();

    /**
     * Indica se o algoritmo possui complexidade quadrática O(n²) no caso médio/pior.
     */
    boolean isQuadratico();

    /**
     * Ordena o vetor fornecido in-place de acordo com o comparador informado.
     *
     * @param dados      vetor de registros a ser ordenado
     * @param comparador critério de ordenação
     */
    void ordenar(ImagemSatelite[] dados, Comparator<ImagemSatelite> comparador);
}
