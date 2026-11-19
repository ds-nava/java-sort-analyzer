package com.sortanalyzer.sort;

import java.util.List;

/**
 * Registro centralizado dos 5 algoritmos de ordenação disponíveis no sistema.
 */
public final class RegistroAlgoritmos {

    private RegistroAlgoritmos() {
    }

    public static List<AlgoritmoOrdenacao> todos() {
        return List.of(
                new BubbleSort(),
                new InsertionSort(),
                new MergeSort(),
                new QuickSort(),
                new HeapSort()
        );
    }
}
