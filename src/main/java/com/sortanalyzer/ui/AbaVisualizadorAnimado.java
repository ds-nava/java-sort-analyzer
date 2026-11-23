package com.sortanalyzer.ui;

import com.sortanalyzer.visual.PainelModoCorrida;
import com.sortanalyzer.visual.PainelModoIndividual;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

/**
 * Aba 2 — Visualizador Animado reunindo o Modo Individual (detalhado passo a passo)
 * e o Modo Corrida Simultânea (5 algoritmos disputando sobre a mesma base).
 */
public final class AbaVisualizadorAnimado extends JPanel {

    private final PainelModoIndividual modoIndividual;
    private final PainelModoCorrida modoCorrida;

    public AbaVisualizadorAnimado() {
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 6, 12));

        modoIndividual = new PainelModoIndividual();
        modoCorrida = new PainelModoCorrida();

        CardLayout cardLayout = new CardLayout();
        JPanel conteudo = new JPanel(cardLayout);
        conteudo.setOpaque(false);
        conteudo.add(modoIndividual, "0");
        conteudo.add(modoCorrida, "1");

        JPanel barraModo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barraModo.setOpaque(false);
        barraModo.add(TemaVisual.criarEtiquetaCampo("MODO DE VISUALIZAÇÃO:"));
        barraModo.add(TemaVisual.criarBarraSegmentada(
                new String[]{"Modo Individual (Passo a Passo)", "Modo Corrida Simultânea (5 Algoritmos)"},
                0,
                idx -> {
                    if (idx == 0) {
                        modoCorrida.pausar();
                    } else {
                        modoIndividual.pausar();
                    }
                    cardLayout.show(conteudo, String.valueOf(idx));
                }
        ));

        add(barraModo, BorderLayout.NORTH);
        add(conteudo, BorderLayout.CENTER);
    }

    public void pausarAnimacoes() {
        modoIndividual.pausar();
        modoCorrida.pausar();
    }
}
