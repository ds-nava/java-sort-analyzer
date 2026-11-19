package com.sortanalyzer.ui;

import com.sortanalyzer.visual.PainelModoCorrida;
import com.sortanalyzer.visual.PainelModoIndividual;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Aba 2 — Visualizador Animado reunindo o Modo Individual (detalhado passo a passo)
 * e o Modo Corrida Simultânea (5 algoritmos disputando sobre a mesma base).
 */
public final class AbaVisualizadorAnimado extends JPanel {

    private final PainelModoIndividual modoIndividual;
    private final PainelModoCorrida modoCorrida;

    public AbaVisualizadorAnimado() {
        setLayout(new BorderLayout());
        setBackground(TemaVisual.FUNDO_JANELA);

        modoIndividual = new PainelModoIndividual();
        modoCorrida = new PainelModoCorrida();

        JTabbedPane subAbas = new JTabbedPane();
        subAbas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        subAbas.addTab("Modo Individual (Passo a Passo)", modoIndividual);
        subAbas.addTab("Modo Corrida Simultânea (5 Algoritmos)", modoCorrida);

        subAbas.addChangeListener(e -> {
            if (subAbas.getSelectedIndex() == 0) {
                modoCorrida.pausar();
            } else {
                modoIndividual.pausar();
            }
        });

        add(subAbas, BorderLayout.CENTER);
    }

    public void pausarAnimacoes() {
        modoIndividual.pausar();
        modoCorrida.pausar();
    }
}
