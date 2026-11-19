package com.sortanalyzer.sort;

import com.sortanalyzer.model.ImagemSatelite;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário de autoverificação para comprovar que a ordenação ocorreu
 * sem erros e sem perda ou duplicação de registros.
 */
public final class VerificadorOrdenacao {

    private VerificadorOrdenacao() {
    }

    /**
     * Verifica se o vetor está em ordem não-decrescente de acordo com o comparador.
     */
    public static boolean estaOrdenado(ImagemSatelite[] dados, Comparator<ImagemSatelite> comparador) {
        if (dados == null || dados.length <= 1) {
            return true;
        }
        for (int i = 1; i < dados.length; i++) {
            if (comparador.compare(dados[i - 1], dados[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se o vetor ordenado contém exatamente os mesmos elementos
     * (multiconjunto) do vetor original.
     */
    public static boolean possuiMesmosElementos(ImagemSatelite[] original, ImagemSatelite[] ordenado) {
        if (original == null && ordenado == null) return true;
        if (original == null || ordenado == null || original.length != ordenado.length) return false;

        Map<ImagemSatelite, Integer> contagem = new HashMap<>(original.length * 2);
        for (ImagemSatelite img : original) {
            contagem.merge(img, 1, Integer::sum);
        }
        for (ImagemSatelite img : ordenado) {
            Integer qtd = contagem.get(img);
            if (qtd == null || qtd == 0) {
                return false;
            }
            if (qtd == 1) {
                contagem.remove(img);
            } else {
                contagem.put(img, qtd - 1);
            }
        }
        return contagem.isEmpty();
    }
}
