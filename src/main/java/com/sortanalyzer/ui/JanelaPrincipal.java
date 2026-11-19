package com.sortanalyzer.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Janela principal do Java Sort Analyzer.
 * Organiza as funcionalidades em 3 abas dedicadas: Análise & Catálogo,
 * Visualizador Animado (Individual e Corrida) e Bateria de Benchmark.
 */
public final class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Java Sort Analyzer — Análise de Performance de Algoritmos de Ordenação");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 700));
        setSize(1240, 790);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        AbaAnaliseCatalogo abaAnalise = new AbaAnaliseCatalogo();
        AbaVisualizadorAnimado abaVisual = new AbaVisualizadorAnimado();
        AbaBateriaBenchmark abaBenchmark = new AbaBateriaBenchmark();

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        abas.addTab("1. Análise & Catálogo Amazônia", abaAnalise);
        abas.addTab("2. Visualizador Animado (Individual & Corrida)", abaVisual);
        abas.addTab("3. Bateria de Benchmark", abaBenchmark);

        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() != 1) {
                abaVisual.pausarAnimacoes();
            }
        });

        add(criarCabecalho(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
    }

    private JPanel criarCabecalho() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(TemaVisual.VERDE_AMAZONIA_ESCURO);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel textos = new JPanel(new BorderLayout(0, 3));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Java Sort Analyzer — Análise de Performance de Algoritmos de Ordenação");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));

        JLabel subtitulo = new JLabel("Monitoramento por Satélite da Amazônia (~100.000 imagens/24h) | Bubble • Insertion • Merge • Quick (Mediana de 3) • Heap");
        subtitulo.setForeground(new Color(204, 251, 241));
        subtitulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        textos.add(titulo, BorderLayout.NORTH);
        textos.add(subtitulo, BorderLayout.SOUTH);

        JButton btnSobre = TemaVisual.criarBotaoSecundario("Ambiente & Metodologia");
        btnSobre.addActionListener(e -> exibirDialogoMetodologia());

        header.add(textos, BorderLayout.CENTER);
        header.add(btnSobre, BorderLayout.EAST);
        return header;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(new Color(226, 232, 240));
        rodape.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));

        JLabel esq = new JLabel("Java Sort Analyzer | Medição isolada via System.nanoTime() (exclui leitura, geração, clonagem e exibição)");
        esq.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        esq.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        JLabel dir = new JLabel("Java " + System.getProperty("java.version") + " | " + System.getProperty("os.name"));
        dir.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        dir.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        rodape.add(esq, BorderLayout.WEST);
        rodape.add(dir, BorderLayout.EAST);
        return rodape;
    }

    private void exibirDialogoMetodologia() {
        Runtime rt = Runtime.getRuntime();
        long maxHeapMb = rt.maxMemory() / (1024 * 1024);
        String msg = String.format(
                "<html><body style='width:460px;font-family:sans-serif;'>"
                        + "<h3>Metodologia de Medição &amp; Ambiente</h3>"
                        + "<p><b>Garantia de Justiça:</b> todos os algoritmos recebem cópias idênticas da mesma base de dados "
                        + "(<code>Arrays.copyOf</code>), com cronômetro isolado via <code>System.nanoTime()</code> medindo "
                        + "exclusivamente o método <code>ordenar()</code>.</p>"
                        + "<p><b>Separação do Visualizador:</b> o motor animado utiliza gravação de passos sobre amostras "
                        + "visuais (10–150 barras) e nunca interfere nas medições de performance.</p>"
                        + "<hr>"
                        + "<p><b>Informações do Ambiente de Execução:</b><br>"
                        + "• Sistema Operacional: %s (%s)<br>"
                        + "• Versão Java: %s (%s)<br>"
                        + "• Núcleos lógicos (CPU): %d<br>"
                        + "• Memória máxima da JVM (Heap): %d MB</p>"
                        + "</body></html>",
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                rt.availableProcessors(),
                maxHeapMb
        );
        JOptionPane.showMessageDialog(this, msg, "Ambiente & Metodologia", JOptionPane.INFORMATION_MESSAGE);
    }
}
