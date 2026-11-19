package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;

/**
 * Implementação do algoritmo Heap Sort utilizando Max-Heap construído in-place
 * e descida iterativa (siftDown), garantindo O(n log n) em todos os cenários
 * com consumo constante de memória auxiliar O(1).
 */
public final class HeapSort implements AlgoritmoOrdenacao {

    @Override
    public String getNome() {
        return "Heap Sort";
    }

    @Override
    public String getComplexidadeMedia() {
        return "O(n log n)";
    }

    @Override
    public String getComplexidadePiorCaso() {
        return "O(n log n)";
    }

    @Override
    public String getComplexidadeMelhorCaso() {
        return "O(n log n)";
    }

    @Override
    public String getComplexidadeEspaco() {
        return "O(1)";
    }

    @Override
    public boolean isEstavel() {
        return false;
    }

    @Override
    public boolean isQuadratico() {
        return false;
    }

    @Override
    public void ordenar(ImagemSatelite[] dados, Comparator<ImagemSatelite> comparador) {
        if (dados == null || dados.length <= 1) {
            return;
        }
        int n = dados.length;

        // 1. Construção do Max-Heap (bottom-up em O(n))
        for (int i = (n / 2) - 1; i >= 0; i--) {
            descerNoHeap(dados, n, i, comparador);
        }

        // 2. Extração sucessiva do maior elemento para o final do vetor
        for (int fim = n - 1; fim > 0; fim--) {
            ImagemSatelite temp = dados[0];
            dados[0] = dados[fim];
            dados[fim] = temp;
            descerNoHeap(dados, fim, 0, comparador);
        }
    }

    private void descerNoHeap(ImagemSatelite[] dados,
                              int tamanhoHeap,
                              int indiceRaiz,
                              Comparator<ImagemSatelite> comparador) {
        int atual = indiceRaiz;
        while (true) {
            int filhoEsquerdo = 2 * atual + 1;
            if (filhoEsquerdo >= tamanhoHeap) {
                break;
            }
            int maior = atual;
            if (comparador.compare(dados[filhoEsquerdo], dados[maior]) > 0) {
                maior = filhoEsquerdo;
            }
            int filhoDireito = filhoEsquerdo + 1;
            if (filhoDireito < tamanhoHeap && comparador.compare(dados[filhoDireito], dados[maior]) > 0) {
                maior = filhoDireito;
            }
            if (maior == atual) {
                break;
            }
            ImagemSatelite temp = dados[atual];
            dados[atual] = dados[maior];
            dados[maior] = temp;
            atual = maior;
        }
    }
}
