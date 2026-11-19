package com.sortanalyzer.visual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gera a trilha determinística de passos visuais para cada um dos 5 algoritmos
 * sobre uma cópia da sequência inicial. Mantém o motor de animação totalmente
 * separado do motor de medição de performance.
 */
public final class GravadorPassosAnimacao {

    public enum TipoAlgoritmoVisual {
        BUBBLE("Bubble Sort", "O(n²)"),
        INSERTION("Insertion Sort", "O(n²)"),
        MERGE("Merge Sort", "O(n log n)"),
        QUICK("Quick Sort (Mediana de 3)", "O(n log n)"),
        HEAP("Heap Sort", "O(n log n)");

        private final String nome;
        private final String complexidade;

        TipoAlgoritmoVisual(String nome, String complexidade) {
            this.nome = nome;
            this.complexidade = complexidade;
        }

        public String getNome() {
            return nome;
        }

        public String getComplexidade() {
            return complexidade;
        }

        @Override
        public String toString() {
            return nome + " [" + complexidade + "]";
        }
    }

    private GravadorPassosAnimacao() {
    }

    public static List<PassoOrdenacao> gravar(TipoAlgoritmoVisual tipo, int[] sequenciaInicial) {
        int[] copia = Arrays.copyOf(sequenciaInicial, sequenciaInicial.length);
        List<PassoOrdenacao> passos = new ArrayList<>();

        switch (tipo) {
            case BUBBLE -> gravarBubble(copia, passos);
            case INSERTION -> gravarInsertion(copia, passos);
            case MERGE -> gravarMerge(copia, passos);
            case QUICK -> gravarQuick(copia, passos);
            case HEAP -> gravarHeap(copia, passos);
        }

        passos.add(PassoOrdenacao.concluido(tipo.getNome() + " finalizado!"));
        return passos;
    }

    private static void gravarBubble(int[] a, List<PassoOrdenacao> passos) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean houveTroca = false;
            int limite = n - 1 - i;
            for (int j = 0; j < limite; j++) {
                passos.add(PassoOrdenacao.comparar(j, j + 1,
                        "Comparando posições [" + j + "]=" + a[j] + " e [" + (j + 1) + "]=" + a[j + 1]));
                if (a[j] > a[j + 1]) {
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                    passos.add(PassoOrdenacao.trocar(j, j + 1,
                            "Trocando [" + j + "] e [" + (j + 1) + "] (" + a[j + 1] + " > " + a[j] + ")"));
                    houveTroca = true;
                }
            }
            passos.add(PassoOrdenacao.marcarOrdenado(limite,
                    "Posição [" + limite + "] fixada com o maior valor restante (" + a[limite] + ")"));
            if (!houveTroca) {
                for (int k = limite - 1; k >= 0; k--) {
                    passos.add(PassoOrdenacao.marcarOrdenado(k,
                            "Nenhuma troca na passada: posição [" + k + "] já está ordenada"));
                }
                break;
            }
        }
        if (n > 0) {
            passos.add(PassoOrdenacao.marcarOrdenado(0, "Posição [0] confirmada"));
        }
    }

    private static void gravarInsertion(int[] a, List<PassoOrdenacao> passos) {
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int chave = a[i];
            int j = i - 1;
            passos.add(PassoOrdenacao.definirPivo(i,
                    "Selecionando chave [" + i + "]=" + chave + " para inserção"));

            while (j >= 0) {
                passos.add(PassoOrdenacao.comparar(j, j + 1,
                        "Comparando [" + j + "]=" + a[j] + " com chave " + chave));
                if (a[j] > chave) {
                    int anterior = a[j + 1];
                    a[j + 1] = a[j];
                    passos.add(PassoOrdenacao.atribuir(j + 1, a[j], anterior,
                            "Deslocando " + a[j] + " da posição [" + j + "] para [" + (j + 1) + "]"));
                    j--;
                } else {
                    break;
                }
            }
            if (a[j + 1] != chave) {
                int anterior = a[j + 1];
                a[j + 1] = chave;
                passos.add(PassoOrdenacao.atribuir(j + 1, chave, anterior,
                        "Inserindo chave " + chave + " na posição [" + (j + 1) + "]"));
            }
        }
        for (int i = 0; i < n; i++) {
            passos.add(PassoOrdenacao.marcarOrdenado(i, "Subvetor totalmente ordenado"));
        }
    }

    private static void gravarMerge(int[] a, List<PassoOrdenacao> passos) {
        if (a.length <= 1) {
            if (a.length == 1) passos.add(PassoOrdenacao.marcarOrdenado(0, "Elemento único ordenado"));
            return;
        }
        int[] aux = new int[a.length];
        mergeRec(a, aux, 0, a.length - 1, passos);
        for (int i = 0; i < a.length; i++) {
            passos.add(PassoOrdenacao.marcarOrdenado(i, "Intercalação final concluída"));
        }
    }

    private static void mergeRec(int[] a, int[] aux, int inicio, int fim, List<PassoOrdenacao> passos) {
        if (inicio >= fim) {
            return;
        }
        int meio = inicio + (fim - inicio) / 2;
        mergeRec(a, aux, inicio, meio, passos);
        mergeRec(a, aux, meio + 1, fim, passos);

        System.arraycopy(a, inicio, aux, inicio, fim - inicio + 1);

        int i = inicio;
        int j = meio + 1;
        int k = inicio;

        while (i <= meio && j <= fim) {
            passos.add(PassoOrdenacao.comparar(i, j,
                    "Intercalando [" + inicio + ".." + fim + "]: comparando " + aux[i] + " e " + aux[j]));
            int escolhido;
            if (aux[i] <= aux[j]) {
                escolhido = aux[i++];
            } else {
                escolhido = aux[j++];
            }
            int anterior = a[k];
            a[k] = escolhido;
            passos.add(PassoOrdenacao.atribuir(k, escolhido, anterior,
                    "Gravando valor " + escolhido + " na posição [" + k + "]"));
            k++;
        }
        while (i <= meio) {
            int anterior = a[k];
            a[k] = aux[i];
            passos.add(PassoOrdenacao.atribuir(k, aux[i], anterior,
                    "Copiando restante da esquerda (" + aux[i] + ") para [" + k + "]"));
            i++;
            k++;
        }
        while (j <= fim) {
            int anterior = a[k];
            a[k] = aux[j];
            passos.add(PassoOrdenacao.atribuir(k, aux[j], anterior,
                    "Copiando restante da direita (" + aux[j] + ") para [" + k + "]"));
            j++;
            k++;
        }
    }

    private static void gravarQuick(int[] a, List<PassoOrdenacao> passos) {
        if (a.length > 1) {
            quickRec(a, 0, a.length - 1, passos);
        }
        for (int i = 0; i < a.length; i++) {
            passos.add(PassoOrdenacao.marcarOrdenado(i, "Partições concluídas"));
        }
    }

    private static void quickRec(int[] a, int inicio, int fim, List<PassoOrdenacao> passos) {
        if (inicio >= fim) {
            if (inicio == fim) {
                passos.add(PassoOrdenacao.marcarOrdenado(inicio, "Subvetor unitário [" + inicio + "] ordenado"));
            }
            return;
        }
        int meio = inicio + (fim - inicio) / 2;
        int idxPivo = indiceMedianaDeTres(a, inicio, meio, fim);
        int valorPivo = a[idxPivo];
        passos.add(PassoOrdenacao.definirPivo(idxPivo,
                "Mediana de 3 em [" + inicio + ".." + fim + "]: pivô=" + valorPivo + " (pos " + idxPivo + ")"));

        int i = inicio - 1;
        int j = fim + 1;
        int p;
        while (true) {
            do {
                i++;
                passos.add(PassoOrdenacao.comparar(i, idxPivo,
                        "Varrendo esquerda: [" + i + "]=" + a[i] + " vs pivô " + valorPivo));
            } while (a[i] < valorPivo);

            do {
                j--;
                passos.add(PassoOrdenacao.comparar(j, idxPivo,
                        "Varrendo direita: [" + j + "]=" + a[j] + " vs pivô " + valorPivo));
            } while (a[j] > valorPivo);

            if (i >= j) {
                p = j;
                break;
            }
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
            if (idxPivo == i) idxPivo = j;
            else if (idxPivo == j) idxPivo = i;

            passos.add(PassoOrdenacao.trocar(i, j,
                    "Particionando: trocando [" + i + "] e [" + j + "]"));
        }

        quickRec(a, inicio, p, passos);
        quickRec(a, p + 1, fim, passos);
    }

    private static int indiceMedianaDeTres(int[] a, int i, int m, int f) {
        int vi = a[i], vm = a[m], vf = a[f];
        if ((vi <= vm && vm <= vf) || (vf <= vm && vm <= vi)) return m;
        if ((vm <= vi && vi <= vf) || (vf <= vi && vi <= vm)) return i;
        return f;
    }

    private static void gravarHeap(int[] a, List<PassoOrdenacao> passos) {
        int n = a.length;
        for (int i = (n / 2) - 1; i >= 0; i--) {
            descerHeapVisual(a, n, i, passos);
        }

        for (int fim = n - 1; fim > 0; fim--) {
            passos.add(PassoOrdenacao.definirPivo(0,
                    "Raiz do Max-Heap (" + a[0] + ") será movida para [" + fim + "]"));
            int tmp = a[0];
            a[0] = a[fim];
            a[fim] = tmp;
            passos.add(PassoOrdenacao.trocar(0, fim,
                    "Extraindo máximo " + a[fim] + " para posição final [" + fim + "]"));
            passos.add(PassoOrdenacao.marcarOrdenado(fim,
                    "Posição [" + fim + "] ordenada definitivamente"));
            descerHeapVisual(a, fim, 0, passos);
        }
        if (n > 0) {
            passos.add(PassoOrdenacao.marcarOrdenado(0, "Raiz final [0] ordenada"));
        }
    }

    private static void descerHeapVisual(int[] a, int tamanhoHeap, int raiz, List<PassoOrdenacao> passos) {
        int atual = raiz;
        while (true) {
            int esq = 2 * atual + 1;
            if (esq >= tamanhoHeap) break;

            int maior = atual;
            passos.add(PassoOrdenacao.comparar(esq, maior,
                    "Heapify: comparando filho esq [" + esq + "]=" + a[esq] + " com [" + maior + "]=" + a[maior]));
            if (a[esq] > a[maior]) {
                maior = esq;
            }

            int dir = esq + 1;
            if (dir < tamanhoHeap) {
                passos.add(PassoOrdenacao.comparar(dir, maior,
                        "Heapify: comparando filho dir [" + dir + "]=" + a[dir] + " com [" + maior + "]=" + a[maior]));
                if (a[dir] > a[maior]) {
                    maior = dir;
                }
            }

            if (maior == atual) break;

            int tmp = a[atual];
            a[atual] = a[maior];
            a[maior] = tmp;
            passos.add(PassoOrdenacao.trocar(atual, maior,
                    "Heapify: trocando pai [" + atual + "] com filho maior [" + maior + "]"));
            atual = maior;
        }
    }
}
