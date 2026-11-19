package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;

/**
 * Implementação do algoritmo Bubble Sort otimizado com flag de parada antecipada.
 * Caso nenhuma troca ocorra em uma varredura completa, o vetor já está ordenado (O(n)).
 */
public final class BubbleSort implements AlgoritmoOrdenacao {

    @Override
    public String getNome() {
        return "Bubble Sort";
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
        for (int i = 0; i < n - 1; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new OrdenacaoCanceladaException();
            }
            boolean houveTroca = false;
            int limite = n - 1 - i;
            for (int j = 0; j < limite; j++) {
                if (comparador.compare(dados[j], dados[j + 1]) > 0) {
                    ImagemSatelite temp = dados[j];
                    dados[j] = dados[j + 1];
                    dados[j + 1] = temp;
                    houveTroca = true;
                }
            }
            if (!houveTroca) {
                break;
            }
        }
    }
}
