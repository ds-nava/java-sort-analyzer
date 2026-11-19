package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;

/**
 * Implementação do algoritmo Insertion Sort por deslocamento.
 * Apresenta desempenho linear O(n) em vetores já ordenados e excelente
 * eficiência em vetores semi-ordenados.
 */
public final class InsertionSort implements AlgoritmoOrdenacao {

    @Override
    public String getNome() {
        return "Insertion Sort";
    }

    @Override
    public String getComplexidadeMedia() {
        return "O(n²)";
    }

    @Override
    public String getComplexidadePiorCaso() {
        return "O(n²)";
    }

    @Override
    public String getComplexidadeMelhorCaso() {
        return "O(n)";
    }

    @Override
    public String getComplexidadeEspaco() {
        return "O(1)";
    }

    @Override
    public boolean isEstavel() {
        return true;
    }

    @Override
    public boolean isQuadratico() {
        return true;
    }

    @Override
    public void ordenar(ImagemSatelite[] dados, Comparator<ImagemSatelite> comparador) {
        if (dados == null || dados.length <= 1) {
            return;
        }
        int n = dados.length;
        for (int i = 1; i < n; i++) {
            if ((i & 255) == 0 && Thread.currentThread().isInterrupted()) {
                throw new OrdenacaoCanceladaException();
            }
            ImagemSatelite chave = dados[i];
            int j = i - 1;
            while (j >= 0 && comparador.compare(dados[j], chave) > 0) {
                dados[j + 1] = dados[j];
                j--;
            }
            dados[j + 1] = chave;
        }
    }
}
