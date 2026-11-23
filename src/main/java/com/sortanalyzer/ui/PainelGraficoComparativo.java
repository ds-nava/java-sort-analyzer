package com.sortanalyzer.ui;

import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.sortanalyzer.model.ResultadoOrdenacao;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gráfico de barras comparativo renderizado nativamente com Java 2D (Graphics2D).
 * Suporta escala linear e logarítmica (ideal para visualizar O(n²) junto de O(n log n)),
 * tema claro/escuro e exportação direta em PNG de alta resolução.
 */
public final class PainelGraficoComparativo extends JPanel {

    private List<ResultadoOrdenacao> resultados = new ArrayList<>();
    private String titulo = "Comparativo de Tempo de Ordenação (ms)";
    private boolean escalaLogaritmica = false;

    public PainelGraficoComparativo() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 270));
        TemaVisual.registrarListenerTema(this::repaint);
    }

    public void setResultados(List<ResultadoOrdenacao> resultados, String titulo) {
        this.resultados = resultados != null ? new ArrayList<>(resultados) : new ArrayList<>();
        if (titulo != null && !titulo.isBlank()) {
            this.titulo = titulo;
        }
        repaint();
    }

    public void setEscalaLogaritmica(boolean escalaLogaritmica) {
        this.escalaLogaritmica = escalaLogaritmica;
        repaint();
    }

    public boolean isEscalaLogaritmica() {
        return escalaLogaritmica;
    }

    /**
     * Exporta o gráfico atual como imagem PNG em alta resolução (sempre em fundo claro para relatórios).
     */
    public void exportarPng(Path destino, int largura, int altura) throws IOException {
        BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            desenharGrafico(g2, largura, altura, false);
        } finally {
            g2.dispose();
        }
        ImageIO.write(img, "png", destino.toFile());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            desenharGrafico(g2, getWidth(), getHeight(), TemaVisual.isModoEscuro());
        } finally {
            g2.dispose();
        }
    }

    private void desenharGrafico(Graphics2D g2, int w, int h, boolean escuro) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fundo = escuro ? TemaVisual.DARK_FUNDO_CARD : TemaVisual.FUNDO_CARD;
        Color textoPrincipal = escuro ? TemaVisual.DARK_TEXTO_PRINCIPAL : TemaVisual.TEXTO_PRINCIPAL;
        Color textoSecundario = escuro ? TemaVisual.DARK_TEXTO_SECUNDARIO : TemaVisual.TEXTO_SECUNDARIO;
        Color corGrade = escuro ? TemaVisual.DARK_BORDA_SUAVE : TemaVisual.BORDA_CLARA;
        Color corEixo = escuro ? new Color(71, 85, 105) : TemaVisual.BORDA_SUAVE;
        Color badgeBg = escuro ? TemaVisual.VERDE_AMAZONIA_ESCURO : new Color(240, 253, 250);
        Color badgeFg = escuro ? TemaVisual.VERDE_DARK_DESTAQUE : TemaVisual.VERDE_AMAZONIA;

        g2.setColor(fundo);
        g2.fillRect(0, 0, w, h);

        // Título + badge de escala
        g2.setColor(textoPrincipal);
        g2.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
        g2.drawString(titulo, 16, 23);

        String tagEscala = escalaLogaritmica ? "Escala Log" : "Escala Linear";
        g2.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 10));
        FontMetrics fmTag = g2.getFontMetrics();
        int tagW = fmTag.stringWidth(tagEscala) + 14;
        int tagX = Math.max(16, w - tagW - 14);
        g2.setColor(badgeBg);
        g2.fillRoundRect(tagX, 10, tagW, 18, 9, 9);
        g2.setColor(badgeFg);
        g2.drawString(tagEscala, tagX + 7, 22);

        if (resultados.isEmpty()) {
            g2.setColor(textoSecundario);
            g2.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 12));
            String msg = "Execute os algoritmos para visualizar o gráfico comparativo.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, Math.max(16, (w - fm.stringWidth(msg)) / 2), h / 2 + 4);
            return;
        }

        int left = 68;
        int right = 20;
        int top = 46;
        int bottom = 46;
        int areaW = Math.max(80, w - left - right);
        int areaH = Math.max(55, h - top - bottom);

        double maxValor = 0.001;
        int idxMaisRapido = -1;
        double menorTempo = Double.MAX_VALUE;
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoOrdenacao r = resultados.get(i);
            double v = transformar(r.getTempoMilissegundos());
            if (v > maxValor) maxValor = v;
            if (r.isOrdenadoCorretamente() && r.getTempoMilissegundos() < menorTempo) {
                menorTempo = r.getTempoMilissegundos();
                idxMaisRapido = i;
            }
        }
        maxValor *= 1.18;

        // Linhas horizontais de grade tracejadas e eixo Y
        g2.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 10));
        FontMetrics fmY = g2.getFontMetrics();
        Stroke originalStroke = g2.getStroke();
        Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4f, new float[]{3f, 3f}, 0f);
        int divisoes = 4;
        for (int d = 0; d <= divisoes; d++) {
            int y = top + areaH - (int) Math.round((areaH * d) / (double) divisoes);
            if (d > 0) {
                g2.setStroke(dashed);
                g2.setColor(corGrade);
                g2.drawLine(left, y, left + areaW, y);
                g2.setStroke(originalStroke);
            }

            double valEixo = (maxValor * d) / divisoes;
            double msReal = escalaLogaritmica ? (Math.pow(10.0, valEixo) - 1.0) : valEixo;
            String rotuloY = formatarEixoMs(msReal);
            g2.setColor(textoSecundario);
            g2.drawString(rotuloY, left - fmY.stringWidth(rotuloY) - 8, y + 4);
        }

        // Eixo X base
        g2.setColor(corEixo);
        g2.drawLine(left, top + areaH, left + areaW, top + areaH);

        // Barras
        int n = resultados.size();
        double slotW = (double) areaW / n;
        int barW = Math.min(76, Math.max(18, (int) (slotW * 0.54)));

        Font fonteValor = new Font(FlatInterFont.FAMILY, Font.BOLD, 10);
        Font fonteLabel = new Font(FlatInterFont.FAMILY, Font.BOLD, 10);
        Font fonteComp = new Font(FlatInterFont.FAMILY, Font.PLAIN, 10);

        for (int i = 0; i < n; i++) {
            ResultadoOrdenacao r = resultados.get(i);
            double v = transformar(r.getTempoMilissegundos());
            int barH = Math.max(4, (int) Math.round((v / maxValor) * areaH));
            int cx = left + (int) Math.round((i + 0.5) * slotW);
            int bx = cx - barW / 2;
            int by = top + areaH - barH;

            g2.setColor(TemaVisual.corAlgoritmo(r.getNomeAlgoritmo()));
            g2.fillRoundRect(bx, by, barW, barH, 6, 6);

            // Rótulo de tempo acima da barra
            g2.setFont(fonteValor);
            String txtMs = String.format(Locale.US, "%.2f ms", r.getTempoMilissegundos());
            if (i == idxMaisRapido && n > 1) {
                txtMs = "★ " + txtMs;
            }
            FontMetrics fmVal = g2.getFontMetrics();
            g2.setColor(i == idxMaisRapido && n > 1 ? badgeFg : textoPrincipal);
            g2.drawString(txtMs, cx - fmVal.stringWidth(txtMs) / 2, by - 6);

            // Rótulo do algoritmo abaixo do eixo X
            g2.setFont(fonteLabel);
            String nomeCurto = encurtarNome(r.getNomeAlgoritmo());
            FontMetrics fmLbl = g2.getFontMetrics();
            g2.setColor(textoPrincipal);
            g2.drawString(nomeCurto, cx - fmLbl.stringWidth(nomeCurto) / 2, top + areaH + 16);

            g2.setFont(fonteComp);
            String comp = r.getComplexidadeMedia();
            FontMetrics fmComp = g2.getFontMetrics();
            g2.setColor(textoSecundario);
            g2.drawString(comp, cx - fmComp.stringWidth(comp) / 2, top + areaH + 30);
        }
    }

    private double transformar(double ms) {
        double seguro = Math.max(0.0, ms);
        return escalaLogaritmica ? Math.log10(seguro + 1.0) : seguro;
    }

    private String formatarEixoMs(double ms) {
        if (ms >= 1000.0) {
            return String.format(Locale.US, "%.1fs", ms / 1000.0);
        }
        if (ms >= 10.0) {
            return String.format(Locale.US, "%.0fms", ms);
        }
        return String.format(Locale.US, "%.2fms", ms);
    }

    private String encurtarNome(String nome) {
        if (nome.startsWith("Quick Sort")) return "Quick Sort";
        return nome;
    }
}
