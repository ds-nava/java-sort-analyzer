package com.sortanalyzer.visual;

import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.sortanalyzer.ui.TemaVisual;

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
 * da animação de ordenação seguindo a identidade visual Teal + Slate de {@link TemaVisual},
 * com suporte automático a Tema Claro e Tema Escuro.
 */
public final class PainelBarrasCanvas extends JPanel {

    public static final Color COR_BARRA_NORMAL = TemaVisual.ANIM_NORMAL;
    public static final Color COR_COMPARANDO = TemaVisual.ANIM_COMPARANDO;
    public static final Color COR_TROCANDO = TemaVisual.ANIM_TROCANDO;
    public static final Color COR_PIVO = TemaVisual.ANIM_PIVO;
    public static final Color COR_ORDENADO = TemaVisual.ANIM_ORDENADO;

    private ReprodutorAnimacao reprodutor;
    private final boolean modoCompacto;
    private int posicaoPodio; // 0 = ainda correndo, 1..5 = posição de chegada

    public PainelBarrasCanvas(boolean modoCompacto) {
        this.modoCompacto = modoCompacto;
        setOpaque(false);
        setDoubleBuffered(true);
        setPreferredSize(modoCompacto ? new Dimension(320, 210) : new Dimension(840, 390));
        TemaVisual.registrarListenerTema(this::repaint);
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

            boolean escuro = TemaVisual.isModoEscuro();
            Color corCard = escuro ? TemaVisual.DARK_FUNDO_CARD : TemaVisual.FUNDO_CARD;
            Color corAreaBarras = escuro ? TemaVisual.DARK_FUNDO_JANELA : TemaVisual.FUNDO_SUTIL;
            Color corTextoSec = escuro ? TemaVisual.DARK_TEXTO_SECUNDARIO : TemaVisual.TEXTO_SECUNDARIO;

            int w = getWidth();
            int h = getHeight();

            // Fundo base do card
            g2.setColor(corCard);
            g2.fillRect(0, 0, w, h);

            if (reprodutor == null) {
                return;
            }

            int alturaTopo = modoCompacto ? 46 : 38;
            desenharCabecalho(g2, w, alturaTopo, escuro);

            // Área de visualização das barras (#F8FAFC no claro, #0F172A no escuro)
            int padArea = modoCompacto ? 8 : 12;
            int areaBoxX = padArea;
            int areaBoxY = alturaTopo + padArea;
            int areaBoxW = Math.max(20, w - 2 * padArea);
            int areaBoxH = Math.max(24, h - alturaTopo - 2 * padArea);

            g2.setColor(corAreaBarras);
            g2.fillRoundRect(areaBoxX, areaBoxY, areaBoxW, areaBoxH, 8, 8);
            g2.setColor(escuro ? TemaVisual.DARK_BORDA_SUAVE : TemaVisual.BORDA_CLARA);
            g2.drawRoundRect(areaBoxX, areaBoxY, areaBoxW - 1, areaBoxH - 1, 8, 8);

            int[] valores = reprodutor.getValoresAtuais();
            if (valores == null || valores.length == 0) {
                return;
            }

            int margemX = areaBoxX + (modoCompacto ? 8 : 14);
            int margemInferior = padArea + (modoCompacto ? 8 : 22);
            int margemSuperior = areaBoxY + (modoCompacto ? 10 : 14);

            int areaW = Math.max(10, w - 2 * margemX);
            int areaH = Math.max(20, h - margemSuperior - margemInferior);

            int maxVal = 1;
            for (int v : valores) {
                if (v > maxVal) maxVal = v;
            }

            int n = valores.length;
            double larguraSlot = (double) areaW / n;
            int espacamento = larguraSlot >= 6.0 ? 2 : (larguraSlot >= 3.0 ? 1 : 0);

            Font fonteValor = new Font(FlatInterFont.FAMILY, Font.BOLD, 10);
            g2.setFont(fonteValor);
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < n; i++) {
                int x = margemX + (int) Math.round(i * larguraSlot);
                int xProx = margemX + (int) Math.round((i + 1) * larguraSlot);
                int barraW = Math.max(1, xProx - x - espacamento);

                int barraH = Math.max(3, (int) Math.round(((double) valores[i] / maxVal) * areaH));
                int y = margemSuperior + (areaH - barraH);

                g2.setColor(escolherCorBarra(i, escuro));
                if (barraW >= 6) {
                    g2.fillRoundRect(x, y, barraW, barraH, 4, 4);
                } else {
                    g2.fillRect(x, y, barraW, barraH);
                }

                if (!modoCompacto && n <= 32 && barraW >= 16) {
                    String rotulo = Integer.toString(valores[i]);
                    int tx = x + (barraW - fm.stringWidth(rotulo)) / 2;
                    g2.setColor(corTextoSec);
                    g2.drawString(rotulo, tx, margemSuperior + areaH + 15);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private void desenharCabecalho(Graphics2D g2, int w, int alturaTopo, boolean escuro) {
        Color corCabecalho = escuro ? TemaVisual.DARK_FUNDO_SUTIL : TemaVisual.FUNDO_JANELA;
        Color corBorda = escuro ? TemaVisual.DARK_BORDA_SUAVE : TemaVisual.BORDA_CLARA;
        Color corTexto = escuro ? TemaVisual.DARK_TEXTO_PRINCIPAL : TemaVisual.TEXTO_PRINCIPAL;
        Color corTextoSec = escuro ? TemaVisual.DARK_TEXTO_SECUNDARIO : TemaVisual.TEXTO_SECUNDARIO;
        Color corProgresso = reprodutor.isConcluido()
                ? COR_ORDENADO
                : (escuro ? TemaVisual.VERDE_DARK_DESTAQUE : TemaVisual.VERDE_AMAZONIA);

        g2.setColor(corCabecalho);
        g2.fillRect(0, 0, w, alturaTopo);

        // Linha divisória inferior discreta
        g2.setColor(corBorda);
        g2.drawLine(0, alturaTopo - 1, w, alturaTopo - 1);

        // Barra fina de progresso na base do cabeçalho
        int progW = (int) Math.round(w * (reprodutor.getProgressoPercentual() / 100.0));
        if (progW > 0) {
            g2.setColor(corProgresso);
            g2.fillRect(0, alturaTopo - 3, progW, 3);
        }

        // Indicador circular da cor do algoritmo (alinhado com tabelas e gráficos)
        String nomeAlg = reprodutor.getAlgoritmo().getNome();
        g2.setColor(TemaVisual.corAlgoritmo(nomeAlg));
        int dotY = modoCompacto ? 10 : 15;
        g2.fillOval(10, dotY, 8, 8);

        g2.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, modoCompacto ? 12 : 13));
        g2.setColor(corTexto);
        String titulo = nomeAlg + "  [" + reprodutor.getAlgoritmo().getComplexidade() + "]";
        g2.drawString(titulo, 24, modoCompacto ? 18 : 23);

        g2.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 11));
        g2.setColor(corTextoSec);
        String metricas = String.format("Comp: %,d  |  Mov: %,d  |  Passo: %,d/%,d",
                reprodutor.getTotalComparacoes(),
                reprodutor.getTotalMovimentacoes(),
                reprodutor.getIndicePasso(),
                reprodutor.getTotalPassos());

        if (modoCompacto) {
            g2.drawString(metricas, 10, 35);
        } else {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(metricas, Math.max(280, w - fm.stringWidth(metricas) - 14), 23);
        }

        if (posicaoPodio > 0) {
            String badge = posicaoPodio + "º LUGAR";
            g2.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 14;
            int bx = w - bw - 8;
            int by = 6;
            Color badgeBg = posicaoPodio == 1
                    ? (escuro ? TemaVisual.VERDE_AMAZONIA : TemaVisual.VERDE_AMAZONIA)
                    : (escuro ? TemaVisual.DARK_BORDA_SUAVE : TemaVisual.BORDA_CLARA);
            Color badgeFg = posicaoPodio == 1 ? Color.WHITE : corTexto;
            g2.setColor(badgeBg);
            g2.fillRoundRect(bx, by, bw, 19, 10, 10);
            g2.setColor(badgeFg);
            g2.drawString(badge, bx + 7, by + 13);
        }
    }

    private Color escolherCorBarra(int indice, boolean escuro) {
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
        return escuro ? TemaVisual.ANIM_NORMAL_DARK : COR_BARRA_NORMAL;
    }
}
