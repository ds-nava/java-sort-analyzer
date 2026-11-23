package com.sortanalyzer.ui;

import com.sortanalyzer.benchmark.ExecutorBenchmark;
import com.sortanalyzer.benchmark.ExecutorBenchmark.ExecucaoIndividual;
import com.sortanalyzer.io.EscritorArquivoCatalogo;
import com.sortanalyzer.io.GeradorDados;
import com.sortanalyzer.io.LeitorArquivoCatalogo;
import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.RelatorioLeitura;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.sort.AlgoritmoOrdenacao;
import com.sortanalyzer.sort.OrdenacaoCanceladaException;
import com.sortanalyzer.sort.RegistroAlgoritmos;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Aba 1 — Análise & Catálogo: aquisição de dados (Arquivo Externo, Gerador Interno
 * ou Entrada Manual), ordenação comparativa sobre cópias idênticas, listagem Antes/Depois
 * paginada e apresentação gráfica dos resultados.
 */
public final class AbaAnaliseCatalogo extends JPanel {

    private final List<AlgoritmoOrdenacao> todosAlgoritmos = RegistroAlgoritmos.todos();
    private final List<JCheckBox> checksAlgoritmos = new ArrayList<>();

    // Controles de fonte interna
    private final JSpinner spinnerQuantidade;
    private final JComboBox<TipoCenario> comboCenarioInterno;
    private final JTextField campoSeed;

    // Controles de entrada manual
    private final JTextArea areaEntradaManual;

    // Controles de arquivo externo
    private final JLabel lblArquivoAtual;

    // Controles de execução
    private final JComboBox<CriterioOrdenacao> comboCriterio;
    private final JButton btnExecutar;
    private final JButton btnCancelar;
    private final JButton btnExportarOrdenado;
    private final JButton btnExportarGrafico;
    private final JCheckBox chkEscalaLog;
    private final JProgressBar barraProgresso;
    private final JLabel lblStatus;

    // Visualizações
    private final PainelTabelaPaginada tabelaAntes;
    private final PainelTabelaPaginada tabelaDepois;
    private final DefaultTableModel modeloResultados;
    private final PainelGraficoComparativo painelGrafico;

    // Estado atual
    private ImagemSatelite[] dadosOriginais = new ImagemSatelite[0];
    private ImagemSatelite[] dadosOrdenados = new ImagemSatelite[0];
    private TipoCenario cenarioAtual = TipoCenario.ALEATORIO;
    private List<ResultadoOrdenacao> ultimosResultados = new ArrayList<>();
    private SwingWorker<List<ResultadoOrdenacao>, ResultadoOrdenacao> workerAtual;

    public AbaAnaliseCatalogo() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1_000, 2, 500_000, 1_000));
        spinnerQuantidade.setPreferredSize(new Dimension(105, 26));
        comboCenarioInterno = new JComboBox<>(TipoCenario.cenariosGeracao());
        campoSeed = new JTextField(Long.toString(GeradorDados.SEED_PADRAO), 6);

        areaEntradaManual = new JTextArea("850, 120, 995, 42, 310, 715, 19, 640, 230, 505", 2, 48);
        areaEntradaManual.setLineWrap(true);
        areaEntradaManual.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        lblArquivoAtual = TemaVisual.criarRotuloSecundario(
                "Nenhum arquivo carregado (aceita catálogo CSV com ';' ou TXT só com números).");

        comboCriterio = new JComboBox<>(CriterioOrdenacao.values());
        btnExecutar = TemaVisual.criarBotaoPrimario("▶  Executar Ordenação & Comparar");
        btnCancelar = TemaVisual.criarBotaoPerigo("Cancelar");
        btnCancelar.setEnabled(false);
        btnExportarOrdenado = TemaVisual.criarBotaoSecundario("Exportar Ordenado (.csv)");
        btnExportarOrdenado.setEnabled(false);
        btnExportarGrafico = TemaVisual.criarBotaoSecundario("Exportar Gráfico (.png)");
        btnExportarGrafico.setEnabled(false);

        chkEscalaLog = new JCheckBox("Escala Log");
        chkEscalaLog.setOpaque(false);

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setStringPainted(true);
        barraProgresso.setPreferredSize(new Dimension(170, 20));

        lblStatus = TemaVisual.criarRotuloStatus("Carregue ou gere uma base de dados para iniciar.");

        tabelaAntes = new PainelTabelaPaginada("Dados ANTES da Ordenação (Base Original)");
        tabelaDepois = new PainelTabelaPaginada("Dados DEPOIS da Ordenação");

        modeloResultados = new DefaultTableModel(
                new Object[]{"Algoritmo", "Média", "Pior Caso", "Estável", "Tempo (ms)", "Validação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        painelGrafico = new PainelGraficoComparativo();

        add(criarPainelSuperior(), BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);

        configurarEventos();

        // Carrega automaticamente uma base inicial de 1.000 registros para demonstração imediata
        carregarBaseInterna(1_000, TipoCenario.ALEATORIO, GeradorDados.SEED_PADRAO);
    }

    private JPanel criarPainelSuperior() {
        JPanel cardUnificado = TemaVisual.criarCard(new BorderLayout(0, 6));
        cardUnificado.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Topo do card: seletor segmentado da fonte + conteúdo dinâmico da fonte
        CardLayout cardLayoutFonte = new CardLayout();
        JPanel painelConteudoFonte = new JPanel(cardLayoutFonte);
        painelConteudoFonte.setOpaque(false);
        painelConteudoFonte.add(criarPainelGeradorInterno(), "0");
        painelConteudoFonte.add(criarPainelArquivoExterno(), "1");
        painelConteudoFonte.add(criarPainelEntradaManual(), "2");

        JPanel linhaFonteHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaFonteHeader.setOpaque(false);
        linhaFonteHeader.add(TemaVisual.criarEtiquetaCampo("FONTE DE DADOS:"));
        linhaFonteHeader.add(TemaVisual.criarBarraSegmentada(
                new String[]{"Gerador Interno", "Arquivo Externo (.csv / .txt)", "Entrada Manual"},
                0,
                idx -> cardLayoutFonte.show(painelConteudoFonte, String.valueOf(idx))
        ));

        JPanel blocoFonte = new JPanel(new BorderLayout(0, 4));
        blocoFonte.setOpaque(false);
        blocoFonte.add(linhaFonteHeader, BorderLayout.NORTH);
        blocoFonte.add(painelConteudoFonte, BorderLayout.CENTER);

        // Separador sutil entre fonte de dados e execução
        JPanel blocoExecucao = new JPanel(new BorderLayout(0, 4));
        blocoExecucao.setOpaque(false);
        blocoExecucao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(148, 163, 184, 70)),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)
        ));

        JPanel linhaAlgs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaAlgs.setOpaque(false);
        linhaAlgs.add(TemaVisual.criarEtiquetaCampo("ALGORITMOS:"));
        for (AlgoritmoOrdenacao alg : todosAlgoritmos) {
            JCheckBox cb = new JCheckBox(alg.getNome(), true);
            cb.setOpaque(false);
            checksAlgoritmos.add(cb);
            linhaAlgs.add(cb);
        }
        linhaAlgs.add(TemaVisual.criarEtiquetaCampo("  CRITÉRIO:"));
        linhaAlgs.add(comboCriterio);

        JPanel linhaAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        linhaAcoes.setOpaque(false);
        linhaAcoes.add(btnExecutar);
        linhaAcoes.add(btnCancelar);
        linhaAcoes.add(btnExportarOrdenado);
        linhaAcoes.add(btnExportarGrafico);
        linhaAcoes.add(chkEscalaLog);

        JPanel linhaStatus = new JPanel(new BorderLayout(10, 0));
        linhaStatus.setOpaque(false);
        linhaStatus.add(barraProgresso, BorderLayout.WEST);
        linhaStatus.add(lblStatus, BorderLayout.CENTER);

        JPanel acoesEStatus = new JPanel(new BorderLayout(0, 5));
        acoesEStatus.setOpaque(false);
        acoesEStatus.add(linhaAcoes, BorderLayout.NORTH);
        acoesEStatus.add(linhaStatus, BorderLayout.SOUTH);

        blocoExecucao.add(linhaAlgs, BorderLayout.NORTH);
        blocoExecucao.add(acoesEStatus, BorderLayout.SOUTH);

        cardUnificado.add(blocoFonte, BorderLayout.NORTH);
        cardUnificado.add(blocoExecucao, BorderLayout.SOUTH);
        return cardUnificado;
    }

    private JPanel criarPainelGeradorInterno() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setOpaque(false);

        JButton btnGerar = TemaVisual.criarBotaoPrimario("Gerar Base");
        JButton btnPreset100k = TemaVisual.criarBotaoSecundario("Simular 100.000 Imagens (24h)");

        btnGerar.addActionListener(e -> acaoGerarInterno());
        btnPreset100k.addActionListener(e -> {
            spinnerQuantidade.setValue(100_000);
            acaoGerarInterno();
        });

        p.add(new JLabel("Quantidade:"));
        p.add(spinnerQuantidade);
        p.add(new JLabel("Disposição inicial:"));
        p.add(comboCenarioInterno);
        p.add(new JLabel("Seed:"));
        p.add(campoSeed);
        p.add(btnGerar);
        p.add(btnPreset100k);
        return p;
    }

    private JPanel criarPainelArquivoExterno() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setOpaque(false);

        JButton btnAbrir = TemaVisual.criarBotaoPrimario("Importar Arquivo (.csv / .txt)...");
        JButton btnGerarExemplos = TemaVisual.criarBotaoSecundario("Gerar Arquivos de Exemplo...");

        btnAbrir.addActionListener(e -> acaoCarregarArquivoExterno());
        btnGerarExemplos.addActionListener(e -> acaoGerarArquivosExemplo());

        p.add(btnAbrir);
        p.add(btnGerarExemplos);
        p.add(lblArquivoAtual);
        return p;
    }

    private JPanel criarPainelEntradaManual() {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);

        JButton btnCarregarManual = TemaVisual.criarBotaoPrimario("Carregar Valores Digitados");
        btnCarregarManual.addActionListener(e -> acaoCarregarEntradaManual());

        JLabel dica = TemaVisual.criarRotuloSecundario(
                "Digite valores numéricos separados por vírgula, espaço ou quebra de linha:");

        p.add(dica, BorderLayout.NORTH);
        p.add(new JScrollPane(areaEntradaManual), BorderLayout.CENTER);
        p.add(btnCarregarManual, BorderLayout.EAST);
        return p;
    }

    private JSplitPane criarPainelCentral() {
        JPanel painelTabelas = new JPanel(new GridLayout(1, 2, 10, 0));
        painelTabelas.setOpaque(false);
        painelTabelas.add(tabelaAntes);
        painelTabelas.add(tabelaDepois);

        JPanel painelResultados = new JPanel(new GridLayout(1, 2, 10, 0));
        painelResultados.setOpaque(false);

        JTable tabelaMetricas = new JTable(modeloResultados);
        TemaVisual.estilizarTabela(tabelaMetricas, 26);
        tabelaMetricas.getColumnModel().getColumn(0).setPreferredWidth(175);
        tabelaMetricas.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabelaMetricas.getColumnModel().getColumn(2).setPreferredWidth(70);
        tabelaMetricas.getColumnModel().getColumn(3).setPreferredWidth(54);
        tabelaMetricas.getColumnModel().getColumn(4).setPreferredWidth(92);
        tabelaMetricas.getColumnModel().getColumn(5).setPreferredWidth(64);

        tabelaMetricas.getColumnModel().getColumn(0).setCellRenderer(TemaVisual.criarRendererAlgoritmo());
        tabelaMetricas.getColumnModel().getColumn(3).setCellRenderer(TemaVisual.criarRendererAlinhado(SwingConstants.CENTER));
        tabelaMetricas.getColumnModel().getColumn(4).setCellRenderer(TemaVisual.criarRendererTempo(this::menorTempoValido));
        tabelaMetricas.getColumnModel().getColumn(5).setCellRenderer(TemaVisual.criarRendererValidacao());

        JPanel cardTabela = TemaVisual.criarCard(new BorderLayout());
        cardTabela.add(TemaVisual.criarCabecalhoCard("Resultados Comparativos (Tempo Exclusivo de Ordenação)"), BorderLayout.NORTH);
        cardTabela.add(TemaVisual.criarScrollLimpo(tabelaMetricas), BorderLayout.CENTER);

        JPanel cardGrafico = TemaVisual.criarCard(new BorderLayout());
        cardGrafico.add(painelGrafico, BorderLayout.CENTER);

        painelResultados.add(cardTabela);
        painelResultados.add(cardGrafico);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelTabelas, painelResultados);
        split.setResizeWeight(0.47);
        split.setDividerLocation(245);
        split.setBorder(null);
        split.putClientProperty(FlatClientProperties.STYLE, "background: null");
        return split;
    }

    private void configurarEventos() {
        btnExecutar.addActionListener(e -> acaoExecutarOrdenacao());
        btnCancelar.addActionListener(e -> {
            if (workerAtual != null && !workerAtual.isDone()) {
                workerAtual.cancel(true);
                lblStatus.setText("Cancelando execução...");
            }
        });
        btnExportarOrdenado.addActionListener(e -> acaoExportarOrdenado());
        btnExportarGrafico.addActionListener(e -> acaoExportarGrafico());
        chkEscalaLog.addActionListener(e -> painelGrafico.setEscalaLogaritmica(chkEscalaLog.isSelected()));
    }

    private void acaoGerarInterno() {
        try {
            int qtd = (Integer) spinnerQuantidade.getValue();
            TipoCenario cenario = (TipoCenario) comboCenarioInterno.getSelectedItem();
            long seed = Long.parseLong(campoSeed.getText().trim());
            carregarBaseInterna(qtd, cenario, seed);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "A seed deve ser um número inteiro válido (ex.: 42).",
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarBaseInterna(int qtd, TipoCenario cenario, long seed) {
        CriterioOrdenacao criterio = (CriterioOrdenacao) comboCriterio.getSelectedItem();
        dadosOriginais = GeradorDados.gerar(qtd, cenario, criterio, seed);
        dadosOrdenados = new ImagemSatelite[0];
        cenarioAtual = cenario;
        ultimosResultados.clear();
        modeloResultados.setRowCount(0);
        tabelaAntes.setDados(dadosOriginais);
        tabelaDepois.setDados(dadosOrdenados);
        painelGrafico.setResultados(ultimosResultados, "Comparativo de Tempo de Ordenação (ms)");
        btnExportarOrdenado.setEnabled(false);
        btnExportarGrafico.setEnabled(false);
        lblStatus.setText(String.format("Base interna gerada: %,d registros (%s, seed=%d).",
                dadosOriginais.length, cenario.getRotulo(), seed));
    }

    private void acaoCarregarArquivoExterno() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Selecionar Arquivo Externo (.csv ou .txt)");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Dados (*.csv, *.txt)", "csv", "txt"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path caminho = chooser.getSelectedFile().toPath();
        try {
            RelatorioLeitura rel = LeitorArquivoCatalogo.ler(caminho);
            dadosOriginais = rel.getRegistros();
            dadosOrdenados = new ImagemSatelite[0];
            cenarioAtual = TipoCenario.EXTERNO;
            ultimosResultados.clear();
            modeloResultados.setRowCount(0);
            tabelaAntes.setDados(dadosOriginais);
            tabelaDepois.setDados(dadosOrdenados);
            painelGrafico.setResultados(ultimosResultados, "Comparativo de Tempo de Ordenação (ms)");
            btnExportarOrdenado.setEnabled(false);
            btnExportarGrafico.setEnabled(false);

            lblArquivoAtual.setText(String.format("Carregado: %s | %,d registros | %s",
                    caminho.getFileName(), dadosOriginais.length, rel.getFormatoDetectado().getDescricao()));
            lblStatus.setText(String.format("Arquivo importado com sucesso: %,d registros válidos.", dadosOriginais.length));

            if (rel.possuiAvisos()) {
                String resumoAvisos = String.join("\n", rel.getAvisos());
                JTextArea area = new JTextArea(resumoAvisos, 12, 52);
                area.setEditable(false);
                JOptionPane.showMessageDialog(this,
                        new Object[]{
                                String.format("Foram carregados %,d registros válidos, porém algumas linhas apresentaram erro e foram ignoradas:",
                                        dadosOriginais.length),
                                new JScrollPane(area)
                        },
                        "Avisos de Leitura do Arquivo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível ler o arquivo selecionado:\n" + ex.getMessage(),
                    "Erro na Leitura Externa", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoGerarArquivosExemplo() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Escolha a pasta para salvar os arquivos de exemplo");

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path pasta = chooser.getSelectedFile().toPath();
        try {
            EscritorArquivoCatalogo.gerarArquivosExemplo(pasta, true);
            JOptionPane.showMessageDialog(this,
                    "Arquivos gerados com sucesso em:\n" + pasta.toAbsolutePath()
                            + "\n\n• catalogo_1000.csv\n• catalogo_10000.csv\n• catalogo_100000.csv\n• numeros_simples.txt\n• arquivo_com_erros.csv",
                    "Arquivos de Exemplo Gerados", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao gerar arquivos de exemplo: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoCarregarEntradaManual() {
        try {
            dadosOriginais = GeradorDados.converterEntradaManual(areaEntradaManual.getText());
            dadosOrdenados = new ImagemSatelite[0];
            cenarioAtual = TipoCenario.MANUAL;
            ultimosResultados.clear();
            modeloResultados.setRowCount(0);
            tabelaAntes.setDados(dadosOriginais);
            tabelaDepois.setDados(dadosOrdenados);
            painelGrafico.setResultados(ultimosResultados, "Comparativo de Tempo de Ordenação (ms)");
            btnExportarOrdenado.setEnabled(false);
            btnExportarGrafico.setEnabled(false);
            lblStatus.setText(String.format("Entrada manual carregada: %,d valores prontos para ordenação.",
                    dadosOriginais.length));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Erro na Entrada Manual", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoExecutarOrdenacao() {
        if (dadosOriginais == null || dadosOriginais.length < 2) {
            JOptionPane.showMessageDialog(this,
                    "Carregue ou gere uma base com pelo menos 2 registros antes de ordenar.",
                    "Sem Dados", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<AlgoritmoOrdenacao> selecionados = new ArrayList<>();
        for (int i = 0; i < checksAlgoritmos.size(); i++) {
            if (checksAlgoritmos.get(i).isSelected()) {
                selecionados.add(todosAlgoritmos.get(i));
            }
        }
        if (selecionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecione pelo menos um algoritmo de ordenação.",
                    "Seleção Vazia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean temQuadraticoGrande = dadosOriginais.length >= 50_000
                && selecionados.stream().anyMatch(AlgoritmoOrdenacao::isQuadratico)
                && cenarioAtual != TipoCenario.ORDENADO;
        if (temQuadraticoGrande) {
            int resp = JOptionPane.showConfirmDialog(this,
                    String.format("Você selecionou algoritmos O(n²) para %,d registros.\n"
                                    + "Isso pode levar alguns segundos (você pode cancelar a qualquer momento).\nDeseja continuar?",
                            dadosOriginais.length),
                    "Aviso de Volume Elevado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (resp != JOptionPane.YES_OPTION) {
                return;
            }
        }

        CriterioOrdenacao criterio = (CriterioOrdenacao) comboCriterio.getSelectedItem();
        modeloResultados.setRowCount(0);
        ultimosResultados = new ArrayList<>();
        barraProgresso.setValue(0);
        btnExecutar.setEnabled(false);
        btnCancelar.setEnabled(true);
        btnExportarOrdenado.setEnabled(false);
        btnExportarGrafico.setEnabled(false);

        workerAtual = new SwingWorker<>() {
            private ImagemSatelite[] ultimoOrdenado;

            @Override
            protected List<ResultadoOrdenacao> doInBackground() {
                List<ResultadoOrdenacao> lista = new ArrayList<>();
                int total = selecionados.size();
                for (int i = 0; i < total; i++) {
                    if (isCancelled()) {
                        throw new OrdenacaoCanceladaException();
                    }
                    AlgoritmoOrdenacao alg = selecionados.get(i);
                    final int idx = i;
                    javax.swing.SwingUtilities.invokeLater(() ->
                            lblStatus.setText(String.format("Ordenando com %s (%d/%d)...", alg.getNome(), idx + 1, total)));

                    ExecucaoIndividual exec = ExecutorBenchmark.executarUmaVez(
                            alg, dadosOriginais, cenarioAtual, criterio);
                    ultimoOrdenado = exec.dadosOrdenados();
                    lista.add(exec.resultado());
                    publish(exec.resultado());
                    setProgress((int) Math.round(((i + 1) * 100.0) / total));
                }
                return lista;
            }

            @Override
            protected void process(List<ResultadoOrdenacao> chunks) {
                for (ResultadoOrdenacao r : chunks) {
                    ultimosResultados.add(r);
                    modeloResultados.addRow(new Object[]{
                            r.getNomeAlgoritmo(),
                            r.getComplexidadeMedia(),
                            r.getComplexidadePiorCaso(),
                            r.isEstavel() ? "Sim" : "Não",
                            String.format(Locale.US, "%.3f", r.getTempoMilissegundos()),
                            r.isOrdenadoCorretamente() ? "OK" : "FALHA"
                    });
                }
                painelGrafico.setResultados(ultimosResultados,
                        String.format("Tempo de Ordenação (n=%,d | %s)", dadosOriginais.length, cenarioAtual.getRotulo()));
            }

            @Override
            protected void done() {
                btnExecutar.setEnabled(true);
                btnCancelar.setEnabled(false);
                if (isCancelled()) {
                    lblStatus.setText("Execução cancelada pelo usuário.");
                    return;
                }
                try {
                    get();
                    if (ultimoOrdenado != null) {
                        dadosOrdenados = ultimoOrdenado;
                        tabelaDepois.setDados(dadosOrdenados);
                        btnExportarOrdenado.setEnabled(true);
                    }
                    btnExportarGrafico.setEnabled(!ultimosResultados.isEmpty());
                    barraProgresso.setValue(100);
                    lblStatus.setText(String.format("Concluído! %,d registros ordenados com sucesso sobre cópias idênticas.",
                            dadosOriginais.length));
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    if (causa instanceof OrdenacaoCanceladaException) {
                        lblStatus.setText("Execução cancelada pelo usuário.");
                    } else {
                        lblStatus.setText("Erro na ordenação: " + causa.getMessage());
                        JOptionPane.showMessageDialog(AbaAnaliseCatalogo.this,
                                "Erro durante a ordenação:\n" + causa.getMessage(),
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

    private void acaoExportarOrdenado() {
        if (dadosOrdenados == null || dadosOrdenados.length == 0) return;

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Salvar Dados Ordenados (.csv)");
        chooser.setSelectedFile(new File("catalogo_ordenado.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destino = chooser.getSelectedFile().toPath();
        try {
            EscritorArquivoCatalogo.exportarCatalogoCsv(destino, dadosOrdenados);
            JOptionPane.showMessageDialog(this,
                    "Catálogo ordenado exportado com sucesso para:\n" + destino.toAbsolutePath(),
                    "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar arquivo: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoExportarGrafico() {
        if (ultimosResultados.isEmpty()) return;

        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Salvar Gráfico Comparativo (.png)");
        chooser.setSelectedFile(new File("grafico_comparativo.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destino = chooser.getSelectedFile().toPath();
        try {
            painelGrafico.exportarPng(destino, 1200, 620);
            JOptionPane.showMessageDialog(this,
                    "Gráfico salvo em alta resolução (1200x620 PNG):\n" + destino.toAbsolutePath(),
                    "Gráfico Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao exportar gráfico: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double menorTempoValido() {
        double min = Double.MAX_VALUE;
        for (ResultadoOrdenacao r : ultimosResultados) {
            if (r.isOrdenadoCorretamente() && r.getTempoMilissegundos() < min) {
                min = r.getTempoMilissegundos();
            }
        }
        return min;
    }
}
