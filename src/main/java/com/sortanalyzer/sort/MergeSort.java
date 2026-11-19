package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;

/**
 * Implementação do algoritmo Merge Sort (divisão e conquista).
 * Garante complexidade O(n log n) em todos os cenários e mantém estabilidade,
 * utilizando um único vetor auxiliar alocado uma única vez.
 */
public final class MergeSort implements AlgoritmoOrdenacao {

    @Override
    public String getNome() {
        return "Merge Sort";
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
        return "O(n)";
    }

    @Override
    public boolean isEstavel() {
        return true;
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
        ImagemSatelite[] auxiliar = new ImagemSatelite[dados.length];
        ordenarIntervalo(dados, auxiliar, 0, dados.length - 1, comparador);
    }

    private void ordenarIntervalo(ImagemSatelite[] dados,
                                  ImagemSatelite[] auxiliar,
                                  int inicio,
                                  int fim,
                                  Comparator<ImagemSatelite> comparador) {
        if (inicio >= fim) {
            return;
        }
        int meio = inicio + (fim - inicio) / 2;
        ordenarIntervalo(dados, auxiliar, inicio, meio, comparador);
        ordenarIntervalo(dados, auxiliar, meio + 1, fim, comparador);

        // Otimização segura: se o maior da esquerda já é <= menor da direita, não há intercalação necessária.
        if (comparador.compare(dados[meio], dados[meio + 1]) <= 0) {
            return;
        }
        intercalar(dados, auxiliar, inicio, meio, fim, comparador);
    }

    private void intercalar(ImagemSatelite[] dados,
                            ImagemSatelite[] auxiliar,
                            int inicio,
                            int meio,
                            int fim,
                            Comparator<ImagemSatelite> comparador) {
        System.arraycopy(dados, inicio, auxiliar, inicio, fim - inicio + 1);

        int i = inicio;
        int j = meio + 1;
        int k = inicio;

        while (i <= meio && j <= fim) {
            if (comparador.compare(auxiliar[i], auxiliar[j]) <= 0) {
                dados[k++] = auxiliar[i++];
            } else {
                dados[k++] = auxiliar[j++];
            }
        }
        while (i <= meio) {
            dados[k++] = auxiliar[i++];
        }
    }
}
