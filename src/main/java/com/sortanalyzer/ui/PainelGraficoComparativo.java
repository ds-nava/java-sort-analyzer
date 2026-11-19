package com.sortanalyzer.ui;

import com.sortanalyzer.model.ResultadoOrdenacao;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gráfico de barras comparativo renderizado nativamente com Java 2D (Graphics2D).
 * Suporta escala linear e logarítmica (ideal para visualizar O(n²) junto de O(n log n))
 * e exportação direta em PNG de alta resolução.
 */
public final class PainelGraficoComparativo extends JPanel {

    private static final Color[] PALETA = {
            new Color(239, 68, 68),   // Bubble Sort (vermelho)
            new Color(245, 158, 11),  // Insertion Sort (âmbar)
            new Color(16, 185, 129),  // Merge Sort (esmeralda)
            new Color(59, 130, 246),  // Quick Sort (azul)
            new Color(139, 92, 246)   // Heap Sort (roxo)
    };

    private List<ResultadoOrdenacao> resultados = new ArrayList<>();
    private String titulo = "Comparativo de Tempo de Ordenação (ms)";
    private boolean escalaLogaritmica = false;

    public PainelGraficoComparativo() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 270));
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
     * Exporta o gráfico atual como imagem PNG em alta resolução.
     */
    public void exportarPng(Path destino, int largura, int altura) throws IOException {
        BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            desenharGrafico(g2, largura, altura);
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
            desenharGrafico(g2, getWidth(), getHeight());
        } finally {
            g2.dispose();
        }
    }

    private void desenharGrafico(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        g2.setColor(TemaVisual.TEXTO_PRINCIPAL);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        String tituloFinal = titulo + (escalaLogaritmica ? "  [Escala Logarítmica]" : "  [Escala Linear]");
        g2.drawString(tituloFinal, 18, 24);

        if (resultados.isEmpty()) {
            g2.setColor(TemaVisual.TEXTO_SECUNDARIO);
            g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            g2.drawString("Execute os algoritmos para visualizar o gráfico comparativo de desempenho.", 18, h / 2);
            return;
        }

        int left = 72;
        int right = 24;
        int top = 46;
        int bottom = 54;
        int areaW = Math.max(80, w - left - right);
        int areaH = Math.max(60, h - top - bottom);

        double maxValor = 0.001;
        for (ResultadoOrdenacao r : resultados) {
            double v = transformar(r.getTempoMilissegundos());
            if (v > maxValor) maxValor = v;
        }
        maxValor *= 1.15;

        // Linhas horizontais de grade e eixo Y
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        FontMetrics fmY = g2.getFontMetrics();
        int divisoes = 4;
        for (int d = 0; d <= divisoes; d++) {
            int y = top + areaH - (int) Math.round((areaH * d) / (double) divisoes);
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(left, y, left + areaW, y);

            double valEixo = (maxValor * d) / divisoes;
            double msReal = escalaLogaritmica ? (Math.pow(10.0, valEixo) - 1.0) : valEixo;
            String rotuloY = formatarEixoMs(msReal);
            g2.setColor(TemaVisual.TEXTO_SECUNDARIO);
            g2.drawString(rotuloY, left - fmY.stringWidth(rotuloY) - 8, y + 4);
        }

        // Eixos X e Y
        g2.setColor(new Color(100, 116, 139));
        g2.drawLine(left, top, left, top + areaH);
        g2.drawLine(left, top + areaH, left + areaW, top + areaH);

        // Barras
        int n = resultados.size();
        double slotW = (double) areaW / n;
        int barW = Math.min(82, Math.max(18, (int) (slotW * 0.56)));

        Font fonteValor = new Font(Font.SANS_SERIF, Font.BOLD, 10);
        Font fonteLabel = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

        for (int i = 0; i < n; i++) {
            ResultadoOrdenacao r = resultados.get(i);
            double v = transformar(r.getTempoMilissegundos());
            int barH = Math.max(3, (int) Math.round((v / maxValor) * areaH));
            int cx = left + (int) Math.round((i + 0.5) * slotW);
            int bx = cx - barW / 2;
            int by = top + areaH - barH;

            g2.setColor(PALETA[i % PALETA.length]);
            g2.fillRoundRect(bx, by, barW, barH, 6, 6);

            // Rótulo de tempo acima da barra
            g2.setFont(fonteValor);
            String txtMs = String.format(Locale.US, "%.2f ms", r.getTempoMilissegundos());
            FontMetrics fmVal = g2.getFontMetrics();
            g2.setColor(TemaVisual.TEXTO_PRINCIPAL);
            g2.drawString(txtMs, cx - fmVal.stringWidth(txtMs) / 2, by - 6);

            // Rótulo do algoritmo abaixo do eixo X
            g2.setFont(fonteLabel);
            String nomeCurto = encurtarNome(r.getNomeAlgoritmo());
            FontMetrics fmLbl = g2.getFontMetrics();
            g2.setColor(TemaVisual.TEXTO_SECUNDARIO);
            g2.drawString(nomeCurto, cx - fmLbl.stringWidth(nomeCurto) / 2, top + areaH + 17);

            String comp = r.getComplexidadeMedia();
            g2.drawString(comp, cx - fmLbl.stringWidth(comp) / 2, top + areaH + 31);
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
