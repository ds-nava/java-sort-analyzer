package com.sortanalyzer.visual;

import com.sortanalyzer.visual.GravadorPassosAnimacao.TipoAlgoritmoVisual;

import java.util.Arrays;
import java.util.List;

/**
 * Controla o estado atual de reprodução de um algoritmo animado, suportando
 * avanço passo-a-passo, retrocesso O(1), reinício e consulta de métricas em tempo real.
 */
public final class ReprodutorAnimacao {

    private final TipoAlgoritmoVisual algoritmo;
    private final int[] valoresIniciais;
    private final List<PassoOrdenacao> passos;

    private int[] valoresAtuais;
    private int[] passoQueOrdenou;
    private int indicePasso;

    private int comparandoA = -1;
    private int comparandoB = -1;
    private int trocandoA = -1;
    private int trocandoB = -1;
    private int escrevendo = -1;
    private int pivo = -1;

    private int totalComparacoes;
    private int totalMovimentacoes;
    private boolean concluido;
    private String descricaoAtual;

    public ReprodutorAnimacao(TipoAlgoritmoVisual algoritmo, int[] sequenciaInicial) {
        this.algoritmo = algoritmo;
        this.valoresIniciais = Arrays.copyOf(sequenciaInicial, sequenciaInicial.length);
        this.passos = GravadorPassosAnimacao.gravar(algoritmo, this.valoresIniciais);
        reiniciar();
    }

    public void reiniciar() {
        this.valoresAtuais = Arrays.copyOf(valoresIniciais, valoresIniciais.length);
        this.passoQueOrdenou = new int[valoresIniciais.length];
        Arrays.fill(this.passoQueOrdenou, -1);
        this.indicePasso = 0;
        limparDestaques();
        this.pivo = -1;
        this.totalComparacoes = 0;
        this.totalMovimentacoes = 0;
        this.concluido = false;
        this.descricaoAtual = "Pronto para iniciar (" + passos.size() + " passos gravados).";
    }

    public boolean avancarUmPasso() {
        if (indicePasso >= passos.size()) {
            return false;
        }
        PassoOrdenacao passo = passos.get(indicePasso);
        aplicarPasso(passo, indicePasso);
        indicePasso++;
        return true;
    }

    public int avancarPassos(int quantidade) {
        int executados = 0;
        for (int i = 0; i < quantidade; i++) {
            if (!avancarUmPasso()) {
                break;
            }
            executados++;
        }
        return executados;
    }

    public boolean voltarUmPasso() {
        if (indicePasso <= 0) {
            return false;
        }
        indicePasso--;
        PassoOrdenacao passo = passos.get(indicePasso);
        desfazerPasso(passo, indicePasso);
        limparDestaques();
        if (indicePasso > 0) {
            PassoOrdenacao anterior = passos.get(indicePasso - 1);
            restaurarDestaquesSomenteVisual(anterior);
            descricaoAtual = anterior.descricao();
        } else {
            pivo = -1;
            descricaoAtual = "Estado inicial restaurado.";
        }
        return true;
    }

    private void aplicarPasso(PassoOrdenacao p, int numPasso) {
        limparDestaques();
        descricaoAtual = p.descricao();

        switch (p.tipo()) {
            case COMPARAR -> {
                comparandoA = p.indiceA();
                comparandoB = p.indiceB();
                totalComparacoes++;
            }
            case TROCAR -> {
                int tmp = valoresAtuais[p.indiceA()];
                valoresAtuais[p.indiceA()] = valoresAtuais[p.indiceB()];
                valoresAtuais[p.indiceB()] = tmp;
                trocandoA = p.indiceA();
                trocandoB = p.indiceB();
                totalMovimentacoes++;
            }
            case ATRIBUIR -> {
                valoresAtuais[p.indiceA()] = p.valorNovo();
                escrevendo = p.indiceA();
                totalMovimentacoes++;
            }
            case DEFINIR_PIVO -> pivo = p.indiceA();
            case MARCAR_ORDENADO -> {
                if (p.indiceA() >= 0 && p.indiceA() < passoQueOrdenou.length && passoQueOrdenou[p.indiceA()] == -1) {
                    passoQueOrdenou[p.indiceA()] = numPasso;
                }
            }
            case CONCLUIDO -> {
                concluido = true;
                pivo = -1;
                for (int i = 0; i < passoQueOrdenou.length; i++) {
                    if (passoQueOrdenou[i] == -1) {
                        passoQueOrdenou[i] = numPasso;
                    }
                }
            }
        }
    }

    private void desfazerPasso(PassoOrdenacao p, int numPasso) {
        switch (p.tipo()) {
            case COMPARAR -> totalComparacoes = Math.max(0, totalComparacoes - 1);
            case TROCAR -> {
                int tmp = valoresAtuais[p.indiceA()];
                valoresAtuais[p.indiceA()] = valoresAtuais[p.indiceB()];
                valoresAtuais[p.indiceB()] = tmp;
                totalMovimentacoes = Math.max(0, totalMovimentacoes - 1);
            }
            case ATRIBUIR -> {
                valoresAtuais[p.indiceA()] = p.valorAnterior();
                totalMovimentacoes = Math.max(0, totalMovimentacoes - 1);
            }
            case DEFINIR_PIVO -> pivo = -1;
            case MARCAR_ORDENADO -> {
                if (p.indiceA() >= 0 && p.indiceA() < passoQueOrdenou.length && passoQueOrdenou[p.indiceA()] == numPasso) {
                    passoQueOrdenou[p.indiceA()] = -1;
                }
            }
            case CONCLUIDO -> {
                concluido = false;
                for (int i = 0; i < passoQueOrdenou.length; i++) {
                    if (passoQueOrdenou[i] == numPasso) {
                        passoQueOrdenou[i] = -1;
                    }
                }
            }
        }
    }

    private void restaurarDestaquesSomenteVisual(PassoOrdenacao p) {
        switch (p.tipo()) {
            case COMPARAR -> {
                comparandoA = p.indiceA();
                comparandoB = p.indiceB();
            }
            case TROCAR -> {
                trocandoA = p.indiceA();
                trocandoB = p.indiceB();
            }
            case ATRIBUIR -> escrevendo = p.indiceA();
            case DEFINIR_PIVO -> pivo = p.indiceA();
            default -> {
            }
        }
    }

    private void limparDestaques() {
        comparandoA = -1;
        comparandoB = -1;
        trocandoA = -1;
        trocandoB = -1;
        escrevendo = -1;
    }

    public TipoAlgoritmoVisual getAlgoritmo() {
        return algoritmo;
    }

    public int[] getValoresAtuais() {
        return valoresAtuais;
    }

    public boolean estaPosicaoOrdenada(int indice) {
        return indice >= 0 && indice < passoQueOrdenou.length && passoQueOrdenou[indice] != -1;
    }

    public int getIndicePasso() {
        return indicePasso;
    }

    public int getTotalPassos() {
        return passos.size();
    }

    public int getComparandoA() {
        return comparandoA;
    }

    public int getComparandoB() {
        return comparandoB;
    }

    public int getTrocandoA() {
        return trocandoA;
    }

    public int getTrocandoB() {
        return trocandoB;
    }

    public int getEscrevendo() {
        return escrevendo;
    }

    public int getPivo() {
        return pivo;
    }

    public int getTotalComparacoes() {
        return totalComparacoes;
    }

    public int getTotalMovimentacoes() {
        return totalMovimentacoes;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public String getDescricaoAtual() {
        return descricaoAtual;
    }

    public double getProgressoPercentual() {
        if (passos.isEmpty()) return 100.0;
        return (100.0 * indicePasso) / passos.size();
    }
}
