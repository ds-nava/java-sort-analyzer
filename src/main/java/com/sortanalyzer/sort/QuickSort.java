package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;

/**
 * Implementação do algoritmo Quick Sort utilizando escolha de pivô por Mediana de Três,
 * particionamento de Hoare e eliminação de recursão de cauda.
 * Essa combinação evita degradação em vetores ordenados/inversos e limita a pilha a O(log n).
 */
public final class QuickSort implements AlgoritmoOrdenacao {

    @Override
    public String getNome() {
        return "Quick Sort (Mediana de 3)";
    }

    @Override
    public String getComplexidadeMedia() {
        return "O(n log n)";
    }

    @Override
    public String getComplexidadePiorCaso() {
        return "O(n²)";
    }

    @Override
    public String getComplexidadeMelhorCaso() {
        return "O(n log n)";
    }

    @Override
    public String getComplexidadeEspaco() {
        return "O(log n)";
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
        ordenarIntervalo(dados, 0, dados.length - 1, comparador);
    }

    private void ordenarIntervalo(ImagemSatelite[] dados,
                                  int inicio,
                                  int fim,
                                  Comparator<ImagemSatelite> comparador) {
        while (inicio < fim) {
            int meio = inicio + (fim - inicio) / 2;
            ImagemSatelite pivo = medianaDeTres(dados[inicio], dados[meio], dados[fim], comparador);
            int p = particionarHoare(dados, inicio, fim, pivo, comparador);

            // Recursão apenas na menor partição para garantir profundidade de pilha O(log n)
            if (p - inicio < fim - (p + 1)) {
                ordenarIntervalo(dados, inicio, p, comparador);
                inicio = p + 1;
            } else {
                ordenarIntervalo(dados, p + 1, fim, comparador);
                fim = p;
            }
        }
    }

    private int particionarHoare(ImagemSatelite[] dados,
                                 int inicio,
                                 int fim,
                                 ImagemSatelite pivo,
                                 Comparator<ImagemSatelite> comparador) {
        int i = inicio - 1;
        int j = fim + 1;
        while (true) {
            do {
                i++;
            } while (comparador.compare(dados[i], pivo) < 0);

            do {
                j--;
            } while (comparador.compare(dados[j], pivo) > 0);

            if (i >= j) {
                return j;
            }
            ImagemSatelite temp = dados[i];
            dados[i] = dados[j];
            dados[j] = temp;
        }
    }

    private ImagemSatelite medianaDeTres(ImagemSatelite a,
                                         ImagemSatelite b,
                                         ImagemSatelite c,
                                         Comparator<ImagemSatelite> comparador) {
        if (comparador.compare(a, b) > 0) {
            ImagemSatelite tmp = a;
            a = b;
            b = tmp;
        }
        if (comparador.compare(b, c) > 0) {
            b = c;
            if (comparador.compare(a, b) > 0) {
                b = a;
            }
        }
        return b;
    }
}
