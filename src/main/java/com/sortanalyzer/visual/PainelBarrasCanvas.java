package com.sortanalyzer.visual;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Componente gráfico Java 2D (Graphics2D) responsável por renderizar as barras
 * da animação de ordenação com destaque por cor para comparações, trocas, pivô e conclusão.
 */
public final class PainelBarrasCanvas extends JPanel {

    public static final Color COR_FUNDO = new Color(15, 23, 42);
    public static final Color COR_CABECALHO = new Color(30, 41, 59);
    public static final Color COR_BARRA_NORMAL = new Color(56, 189, 248);
    public static final Color COR_COMPARANDO = new Color(251, 191, 36);
    public static final Color COR_TROCANDO = new Color(244, 63, 94);
    public static final Color COR_PIVO = new Color(168, 85, 247);
    public static final Color COR_ORDENADO = new Color(16, 185, 129);
    public static final Color COR_TEXTO = new Color(241, 245, 249);
    public static final Color COR_TEXTO_SUAVE = new Color(148, 163, 184);

    private ReprodutorAnimacao reprodutor;
    private final boolean modoCompacto;
    private int posicaoPodio; // 0 = ainda correndo, 1..5 = posição de chegada

    public PainelBarrasCanvas(boolean modoCompacto) {
        this.modoCompacto = modoCompacto;
        setBackground(COR_FUNDO);
        setDoubleBuffered(true);
        setPreferredSize(modoCompacto ? new Dimension(320, 210) : new Dimension(840, 390));
    }

    public void setReprodutor(ReprodutorAnimacao reprodutor) {
        this.reprodutor = reprodutor;
        this.posicaoPodio = 0;
        repaint();
    }

    public ReprodutorAnimacao getReprodutor() {
        return reprodutor;
    }

    public void setPosicaoPodio(int posicaoPodio) {
        this.posicaoPodio = posicaoPodio;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(COR_FUNDO);
            g2.fillRect(0, 0, w, h);

            if (reprodutor == null) {
                return;
            }

            int alturaTopo = modoCompacto ? 44 : 36;
            desenharCabecalho(g2, w, alturaTopo);

            int[] valores = reprodutor.getValoresAtuais();
            if (valores == null || valores.length == 0) {
                return;
            }

            int margemX = modoCompacto ? 10 : 18;
            int margemInferior = modoCompacto ? 10 : 24;
            int margemSuperior = alturaTopo + 12;

            int areaW = Math.max(10, w - 2 * margemX);
            int areaH = Math.max(20, h - margemSuperior - margemInferior);

            int maxVal = 1;
            for (int v : valores) {
                if (v > maxVal) maxVal = v;
            }

            int n = valores.length;
            double larguraSlot = (double) areaW / n;
            int espacamento = larguraSlot >= 6.0 ? 2 : (larguraSlot >= 3.0 ? 1 : 0);

            Font fonteValor = new Font(Font.SANS_SERIF, Font.BOLD, 10);
            g2.setFont(fonteValor);
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < n; i++) {
                int x = margemX + (int) Math.round(i * larguraSlot);
                int xProx = margemX + (int) Math.round((i + 1) * larguraSlot);
                int barraW = Math.max(1, xProx - x - espacamento);

                int barraH = Math.max(3, (int) Math.round(((double) valores[i] / maxVal) * areaH));
                int y = margemSuperior + (areaH - barraH);

                g2.setColor(escolherCorBarra(i));
                if (barraW >= 6) {
                    g2.fillRoundRect(x, y, barraW, barraH, 4, 4);
                } else {
                    g2.fillRect(x, y, barraW, barraH);
                }

                if (!modoCompacto && n <= 32 && barraW >= 16) {
                    String rotulo = Integer.toString(valores[i]);
                    int tx = x + (barraW - fm.stringWidth(rotulo)) / 2;
                    g2.setColor(COR_TEXTO_SUAVE);
                    g2.drawString(rotulo, tx, margemSuperior + areaH + 15);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private void desenharCabecalho(Graphics2D g2, int w, int alturaTopo) {
        g2.setColor(COR_CABECALHO);
        g2.fillRect(0, 0, w, alturaTopo);

        // Barra fina de progresso na base do cabeçalho
        int progW = (int) Math.round(w * (reprodutor.getProgressoPercentual() / 100.0));
        g2.setColor(reprodutor.isConcluido() ? COR_ORDENADO : COR_BARRA_NORMAL);
        g2.fillRect(0, alturaTopo - 3, progW, 3);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, modoCompacto ? 12 : 13));
        g2.setColor(COR_TEXTO);
        String titulo = reprodutor.getAlgoritmo().getNome()
                + "  [" + reprodutor.getAlgoritmo().getComplexidade() + "]";
        g2.drawString(titulo, 10, modoCompacto ? 17 : 22);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2.setColor(COR_TEXTO_SUAVE);
        String metricas = String.format("Comp: %,d  |  Mov: %,d  |  Passo: %,d/%,d",
                reprodutor.getTotalComparacoes(),
                reprodutor.getTotalMovimentacoes(),
                reprodutor.getIndicePasso(),
                reprodutor.getTotalPassos());

        if (modoCompacto) {
            g2.drawString(metricas, 10, 34);
        } else {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(metricas, Math.max(280, w - fm.stringWidth(metricas) - 14), 22);
        }

        if (posicaoPodio > 0) {
            String badge = posicaoPodio + "º LUGAR";
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 14;
            int bx = w - bw - 8;
            int by = 6;
            g2.setColor(posicaoPodio == 1 ? new Color(234, 179, 8) : COR_ORDENADO);
            g2.fillRoundRect(bx, by, bw, 20, 10, 10);
            g2.setColor(COR_FUNDO);
            g2.drawString(badge, bx + 7, by + 14);
        }
    }

    private Color escolherCorBarra(int indice) {
        if (reprodutor.isConcluido()) {
            return COR_ORDENADO;
        }
        if (indice == reprodutor.getTrocandoA()
                || indice == reprodutor.getTrocandoB()
                || indice == reprodutor.getEscrevendo()) {
            return COR_TROCANDO;
        }
        if (indice == reprodutor.getComparandoA() || indice == reprodutor.getComparandoB()) {
            return COR_COMPARANDO;
        }
        if (indice == reprodutor.getPivo()) {
            return COR_PIVO;
        }
        if (reprodutor.estaPosicaoOrdenada(indice)) {
            return COR_ORDENADO;
        }
        return COR_BARRA_NORMAL;
    }
}
