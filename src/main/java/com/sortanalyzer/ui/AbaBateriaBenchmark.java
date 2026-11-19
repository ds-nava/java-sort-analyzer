package com.sortanalyzer.ui;

import com.sortanalyzer.benchmark.BateriaBenchmark;
import com.sortanalyzer.benchmark.ConfiguracaoBateria;
import com.sortanalyzer.io.EscritorArquivoCatalogo;
import com.sortanalyzer.io.GeradorDados;
import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.sort.AlgoritmoOrdenacao;
import com.sortanalyzer.sort.OrdenacaoCanceladaException;
import com.sortanalyzer.sort.RegistroAlgoritmos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Aba 3 — Bateria de Benchmark automatizada para execução de matrizes
 * de Tamanhos × Cenários × Algoritmos com exportação em CSV e PNG.
 */
public final class AbaBateriaBenchmark extends JPanel {

    private static final int[] TAMANHOS_PADRAO = {1_000, 10_000, 50_000, 100_000};

    private final List<AlgoritmoOrdenacao> todosAlgoritmos = RegistroAlgoritmos.todos();
    private final List<JCheckBox> checksTamanhos = new ArrayList<>();
    private final List<JCheckBox> checksCenarios = new ArrayList<>();
    private final List<JCheckBox> checksAlgoritmos = new ArrayList<>();

    private final JComboBox<CriterioOrdenacao> comboCriterio;
    private final JSpinner spinnerRepeticoes;
    private final JTextField campoSeed;
    private final JCheckBox chkRepeticoesInteligentes;
    private final JCheckBox chkWarmUp;

    private final JButton btnExecutar;
    private final JButton btnCancelar;
    private final JButton btnExportarCsv;
    private final JButton btnExportarPng;
    private final JProgressBar barraProgresso;
    private final JLabel lblStatus;

    private final DefaultTableModel modeloTabela;
    private final JComboBox<Integer> filtroTamanhoGrafico;
    private final JComboBox<TipoCenario> filtroCenarioGrafico;
    private final JCheckBox chkEscalaLog;
    private final PainelGraficoComparativo painelGrafico;

    private List<ResultadoOrdenacao> resultados = new ArrayList<>();
    private SwingWorker<List<ResultadoOrdenacao>, ResultadoOrdenacao> workerAtual;

    public AbaBateriaBenchmark() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(TemaVisual.FUNDO_JANELA);

        comboCriterio = new JComboBox<>(CriterioOrdenacao.values());
        spinnerRepeticoes = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        campoSeed = new JTextField(Long.toString(GeradorDados.SEED_PADRAO), 5);

        chkRepeticoesInteligentes = new JCheckBox("Repetições inteligentes (1 rep. p/ O(n²) ≥ 25k)", true);
        chkRepeticoesInteligentes.setOpaque(false);
        chkRepeticoesInteligentes.setToolTipText(
                "Evita esperas longas executando 1 rodada cronometrada para Bubble/Insertion em 50k e 100k, mantendo a média de repetições nos demais testes.");

        chkWarmUp = new JCheckBox("Aquecimento JVM (Warm-up)", true);
        chkWarmUp.setOpaque(false);

        btnExecutar = TemaVisual.criarBotaoPrimario("Executar Bateria");
        btnCancelar = TemaVisual.criarBotaoPerigo("Cancelar");
        btnCancelar.setEnabled(false);
        btnExportarCsv = TemaVisual.criarBotaoSecundario("Exportar CSV");
        btnExportarCsv.setEnabled(false);
        btnExportarPng = TemaVisual.criarBotaoSecundario("Exportar Gráfico (.png)");
        btnExportarPng.setEnabled(false);

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setStringPainted(true);
        barraProgresso.setPreferredSize(new Dimension(180, 22));

        lblStatus = new JLabel("Configure a matriz de testes e clique em Executar Bateria.");
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblStatus.setForeground(TemaVisual.VERDE_AMAZONIA_ESCURO);

        modeloTabela = new DefaultTableModel(
                new Object[]{"Tamanho", "Cenário", "Algoritmo", "Complexidade", "Tempo Médio (ms)", "Reps", "Validação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        filtroTamanhoGrafico = new JComboBox<>();
        filtroCenarioGrafico = new JComboBox<>(TipoCenario.cenariosGeracao());
        chkEscalaLog = new JCheckBox("Escala Log", true);
        chkEscalaLog.setOpaque(false);

        painelGrafico = new PainelGraficoComparativo();
        painelGrafico.setEscalaLogaritmica(true);

        add(criarPainelConfiguracao(), BorderLayout.NORTH);
        add(criarPainelResultados(), BorderLayout.CENTER);

        configurarEventos();
    }

    private JPanel criarPainelConfiguracao() {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.BORDA_SUAVE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 1));
        linha1.setOpaque(false);
        linha1.add(new JLabel("Tamanhos:"));
        for (int tam : TAMANHOS_PADRAO) {
            JCheckBox cb = new JCheckBox(String.format("%,d", tam), true);
            cb.setOpaque(false);
            checksTamanhos.add(cb);
            linha1.add(cb);
        }
        linha1.add(new JLabel("  Critério:"));
        linha1.add(comboCriterio);
        linha1.add(new JLabel("  Reps:"));
        linha1.add(spinnerRepeticoes);
        linha1.add(new JLabel("  Seed:"));
        linha1.add(campoSeed);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 1));
        linha2.setOpaque(false);
        linha2.add(new JLabel("Cenários:"));
        for (TipoCenario c : TipoCenario.cenariosGeracao()) {
            JCheckBox cb = new JCheckBox(c.getRotulo(), true);
            cb.setOpaque(false);
            checksCenarios.add(cb);
            linha2.add(cb);
        }
        linha2.add(chkWarmUp);

        JPanel linha3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 1));
        linha3.setOpaque(false);
        linha3.add(new JLabel("Algoritmos:"));
        for (AlgoritmoOrdenacao alg : todosAlgoritmos) {
            JCheckBox cb = new JCheckBox(alg.getNome(), true);
            cb.setOpaque(false);
            checksAlgoritmos.add(cb);
            linha3.add(cb);
        }
        linha3.add(chkRepeticoesInteligentes);

        JPanel linha4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        linha4.setOpaque(false);
        JButton btnPresetRapido = TemaVisual.criarBotaoSecundario("Preset Rápido (1k–10k)");
        JButton btnPresetCompleto = TemaVisual.criarBotaoSecundario("Preset Completo (1k–100k)");
        btnPresetRapido.addActionListener(e -> aplicarPreset(false));
        btnPresetCompleto.addActionListener(e -> aplicarPreset(true));

        linha4.add(btnExecutar);
        linha4.add(btnCancelar);
        linha4.add(btnPresetRapido);
        linha4.add(btnPresetCompleto);
        linha4.add(btnExportarCsv);
        linha4.add(btnExportarPng);

        JPanel linhaStatus = new JPanel(new BorderLayout(10, 0));
        linhaStatus.setOpaque(false);
        linhaStatus.setBorder(BorderFactory.createEmptyBorder(3, 4, 0, 4));
        linhaStatus.add(barraProgresso, BorderLayout.WEST);
        linhaStatus.add(lblStatus, BorderLayout.CENTER);

        JPanel linhas = new JPanel(new java.awt.GridLayout(4, 1, 0, 2));
        linhas.setOpaque(false);
        linhas.add(linha1);
        linhas.add(linha2);
        linhas.add(linha3);
        linhas.add(linha4);

        card.add(linhas, BorderLayout.CENTER);
        card.add(linhaStatus, BorderLayout.SOUTH);
        return card;
    }

    private JSplitPane criarPainelResultados() {
        JTable tabela = new JTable(modeloTabela);
        tabela.setRowHeight(23);
        tabela.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));

        JPanel cardTabela = new JPanel(new BorderLayout());
        cardTabela.setBackground(Color.WHITE);
        cardTabela.setBorder(BorderFactory.createLineBorder(TemaVisual.BORDA_SUAVE));
        JLabel titTab = new JLabel("  Resultados Consolidados da Bateria");
        titTab.setOpaque(true);
        titTab.setBackground(TemaVisual.VERDE_AMAZONIA_ESCURO);
        titTab.setForeground(Color.WHITE);
        titTab.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titTab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        cardTabela.add(titTab, BorderLayout.NORTH);
        cardTabela.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel cardGrafico = new JPanel(new BorderLayout());
        cardGrafico.setBackground(Color.WHITE);
        cardGrafico.setBorder(BorderFactory.createLineBorder(TemaVisual.BORDA_SUAVE));

        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barraFiltro.setBackground(new Color(248, 250, 252));
        barraFiltro.add(new JLabel("Filtrar gráfico — Tamanho:"));
        barraFiltro.add(filtroTamanhoGrafico);
        barraFiltro.add(new JLabel("Cenário:"));
        barraFiltro.add(filtroCenarioGrafico);
        barraFiltro.add(chkEscalaLog);

        cardGrafico.add(barraFiltro, BorderLayout.NORTH);
        cardGrafico.add(painelGrafico, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cardTabela, cardGrafico);
        split.setResizeWeight(0.48);
        split.setDividerLocation(230);
        split.setBorder(null);
        return split;
    }

    private void configurarEventos() {
        btnExecutar.addActionListener(e -> iniciarBateria());
        btnCancelar.addActionListener(e -> {
            if (workerAtual != null && !workerAtual.isDone()) {
                workerAtual.cancel(true);
                lblStatus.setText("Cancelando bateria de benchmark...");
            }
        });
        btnExportarCsv.addActionListener(e -> exportarCsv());
        btnExportarPng.addActionListener(e -> exportarPng());

        filtroTamanhoGrafico.addActionListener(e -> atualizarGraficoFiltrado());
        filtroCenarioGrafico.addActionListener(e -> atualizarGraficoFiltrado());
        chkEscalaLog.addActionListener(e -> painelGrafico.setEscalaLogaritmica(chkEscalaLog.isSelected()));
    }

    private void aplicarPreset(boolean completo) {
        for (int i = 0; i < TAMANHOS_PADRAO.length; i++) {
            checksTamanhos.get(i).setSelected(completo || TAMANHOS_PADRAO[i] <= 10_000);
        }
        for (JCheckBox cb : checksCenarios) cb.setSelected(true);
        for (JCheckBox cb : checksAlgoritmos) cb.setSelected(true);
        chkRepeticoesInteligentes.setSelected(true);
        lblStatus.setText(completo
                ? "Preset Completo aplicado (1.000 a 100.000 registros × 4 cenários × 5 algoritmos)."
                : "Preset Rápido aplicado (1.000 e 10.000 registros × 4 cenários × 5 algoritmos).");
    }

    private void iniciarBateria() {
        List<Integer> tamanhos = new ArrayList<>();
        for (int i = 0; i < checksTamanhos.size(); i++) {
            if (checksTamanhos.get(i).isSelected()) tamanhos.add(TAMANHOS_PADRAO[i]);
        }

        List<TipoCenario> cenarios = new ArrayList<>();
        TipoCenario[] todosCenarios = TipoCenario.cenariosGeracao();
        for (int i = 0; i < checksCenarios.size(); i++) {
            if (checksCenarios.get(i).isSelected()) cenarios.add(todosCenarios[i]);
        }

        List<AlgoritmoOrdenacao> algoritmos = new ArrayList<>();
        for (int i = 0; i < checksAlgoritmos.size(); i++) {
            if (checksAlgoritmos.get(i).isSelected()) algoritmos.add(todosAlgoritmos.get(i));
        }

        long seed;
        try {
            seed = Long.parseLong(campoSeed.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seed inválida. Digite um número inteiro.",
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ConfiguracaoBateria config;
        try {
            config = new ConfiguracaoBateria(
                    tamanhos,
                    cenarios,
                    algoritmos,
                    (CriterioOrdenacao) comboCriterio.getSelectedItem(),
                    seed,
                    (Integer) spinnerRepeticoes.getValue(),
                    chkRepeticoesInteligentes.isSelected(),
                    chkWarmUp.isSelected()
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Configuração Incompleta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resultados = new ArrayList<>();
        modeloTabela.setRowCount(0);
        filtroTamanhoGrafico.removeAllItems();
        for (Integer t : tamanhos) filtroTamanhoGrafico.addItem(t);
        if (!cenarios.isEmpty()) filtroCenarioGrafico.setSelectedItem(cenarios.get(0));

        barraProgresso.setValue(0);
        btnExecutar.setEnabled(false);
        btnCancelar.setEnabled(true);
        btnExportarCsv.setEnabled(false);
        btnExportarPng.setEnabled(false);

        workerAtual = new SwingWorker<>() {
            @Override
            protected List<ResultadoOrdenacao> doInBackground() {
                return BateriaBenchmark.executar(
                        config,
                        (concluidos, total, msg, novoRes) -> {
                            int pct = total > 0 ? (int) Math.round((concluidos * 100.0) / total) : 0;
                            setProgress(pct);
                            SwingUtilities.invokeLater(() -> lblStatus.setText(msg));
                            if (novoRes != null) {
                                publish(novoRes);
                            }
                        },
                        this::isCancelled
                );
            }

            @Override
            protected void process(List<ResultadoOrdenacao> chunks) {
                for (ResultadoOrdenacao r : chunks) {
                    resultados.add(r);
                    modeloTabela.addRow(new Object[]{
                            String.format("%,d", r.getTamanhoEntrada()),
                            r.getCenario().getRotulo(),
                            r.getNomeAlgoritmo(),
                            r.getComplexidadeMedia(),
                            String.format(Locale.US, "%.3f", r.getTempoMilissegundos()),
                            r.getRepeticoes(),
                            r.isOrdenadoCorretamente() ? "OK" : "FALHA"
                    });
                }
                atualizarGraficoFiltrado();
            }

            @Override
            protected void done() {
                btnExecutar.setEnabled(true);
                btnCancelar.setEnabled(false);
                boolean temDados = !resultados.isEmpty();
                btnExportarCsv.setEnabled(temDados);
                btnExportarPng.setEnabled(temDados);

                if (isCancelled()) {
                    lblStatus.setText("Bateria cancelada. Resultados parciais mantidos na tabela.");
                    return;
                }
                try {
                    get();
                    barraProgresso.setValue(100);
                    lblStatus.setText(String.format("Bateria concluída! %d medições prontas para exportação em CSV/PNG.",
                            resultados.size()));
                    atualizarGraficoFiltrado();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    if (causa instanceof OrdenacaoCanceladaException) {
                        lblStatus.setText("Bateria cancelada pelo usuário.");
                    } else {
                        lblStatus.setText("Erro na bateria: " + causa.getMessage());
                        JOptionPane.showMessageDialog(AbaBateriaBenchmark.this,
                                "Erro ao executar bateria:\n" + causa.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        workerAtual.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                barraProgresso.setValue((Integer) evt.getNewValue());
            }
        });
        workerAtual.execute();
    }

    private void atualizarGraficoFiltrado() {
        Integer tam = (Integer) filtroTamanhoGrafico.getSelectedItem();
        TipoCenario cen = (TipoCenario) filtroCenarioGrafico.getSelectedItem();
        if (tam == null || cen == null) {
            return;
        }
        List<ResultadoOrdenacao> filtrados = new ArrayList<>();
        for (ResultadoOrdenacao r : resultados) {
            if (r.getTamanhoEntrada() == tam && r.getCenario() == cen) {
                filtrados.add(r);
            }
        }
        painelGrafico.setResultados(filtrados,
                String.format("Benchmark — n=%,d registros | Cenário: %s", tam, cen.getRotulo()));
    }

    private void exportarCsv() {
        if (resultados.isEmpty()) return;

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Exportar Resultados do Benchmark (.csv)");
        chooser.setSelectedFile(new File("benchmark_resultados.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destino = chooser.getSelectedFile().toPath();
        try {
            EscritorArquivoCatalogo.exportarResultadosCsv(destino, resultados);
            JOptionPane.showMessageDialog(this,
                    "Resultados exportados em CSV (pronto para Excel/Google Sheets):\n" + destino.toAbsolutePath(),
                    "CSV Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar CSV: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarPng() {
        if (resultados.isEmpty()) return;

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Exportar Gráfico Filtrado (.png)");
        chooser.setSelectedFile(new File("benchmark_grafico.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destino = chooser.getSelectedFile().toPath();
        try {
            painelGrafico.exportarPng(destino, 1200, 620);
            JOptionPane.showMessageDialog(this,
                    "Gráfico exportado em PNG (1200x620):\n" + destino.toAbsolutePath(),
                    "Gráfico Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar PNG: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
