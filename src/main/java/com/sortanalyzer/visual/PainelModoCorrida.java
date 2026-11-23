package com.sortanalyzer.visual;

import com.sortanalyzer.io.GeradorDados;
import com.sortanalyzer.model.TipoCenario;
import com.sortanalyzer.ui.TemaVisual;
import com.sortanalyzer.visual.GravadorPassosAnimacao.TipoAlgoritmoVisual;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modo Corrida Simultânea: executa todos os 5 algoritmos em paralelo visual
 * sobre cópias idênticas da mesma sequência numérica, exibindo o pódio em tempo real.
 */
public final class PainelModoCorrida extends JPanel {

    private final JComboBox<TipoCenario> comboCenario;
    private final JSlider sliderQuantidade;
    private final JLabel lblQuantidade;
    private final JSlider sliderVelocidade;
    private final JLabel lblVelocidade;

    private final JButton btnIniciarPausar;
    private final JButton btnReiniciar;
    private final JButton btnNovaSequencia;

    private final List<PainelBarrasCanvas> paineis = new ArrayList<>();
    private final List<ReprodutorAnimacao> reprodutores = new ArrayList<>();
    private final DefaultTableModel modeloPodio;

    private final Timer timer;
    private final Random geradorSeeds = new Random();
    private long seedAtual = GeradorDados.SEED_PADRAO;
    private int proximoLugarPodio = 1;

    public PainelModoCorrida() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);

        comboCenario = new JComboBox<>(TipoCenario.cenariosGeracao());

        sliderQuantidade = new JSlider(15, 100, 45);
        sliderQuantidade.setOpaque(false);
        lblQuantidade = new JLabel("Barras: 45");

        sliderVelocidade = new JSlider(1, 100, 65);
        sliderVelocidade.setOpaque(false);
        lblVelocidade = new JLabel("Velocidade: 65%");

        btnIniciarPausar = TemaVisual.criarBotaoPrimario("▶  Iniciar Corrida");
        btnReiniciar = TemaVisual.criarBotaoSecundario("Reiniciar");
        btnNovaSequencia = TemaVisual.criarBotaoSecundario("Nova Sequência");

        modeloPodio = new DefaultTableModel(
                new Object[]{"#", "Algoritmo", "Passos", "Comp.", "Mov."}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        add(criarBarraControles(), BorderLayout.NORTH);
        add(criarGradeCorrida(), BorderLayout.CENTER);
        add(PainelModoIndividual.criarLegendaCores(), BorderLayout.SOUTH);

        timer = new Timer(25, e -> aoTickCorrida());

        configurarEventos();
        recarregarCorrida();
    }

    private JPanel criarBarraControles() {
        JPanel card = TemaVisual.criarCard(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        sliderQuantidade.setPreferredSize(new java.awt.Dimension(130, 24));
        sliderVelocidade.setPreferredSize(new java.awt.Dimension(130, 24));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linha1.setOpaque(false);
        linha1.add(TemaVisual.criarEtiquetaCampo("CENÁRIO (MESMA BASE):"));
        linha1.add(comboCenario);
        linha1.add(lblQuantidade);
        linha1.add(sliderQuantidade);
        linha1.add(lblVelocidade);
        linha1.add(sliderVelocidade);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha2.setOpaque(false);
        linha2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(148, 163, 184, 70)),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)
        ));
        linha2.add(btnIniciarPausar);
        linha2.add(btnReiniciar);
        linha2.add(btnNovaSequencia);

        card.add(linha1, BorderLayout.NORTH);
        card.add(linha2, BorderLayout.SOUTH);
        return card;
    }

    private JPanel criarGradeCorrida() {
        JPanel grade = new JPanel(new GridLayout(2, 3, 10, 10));
        grade.setOpaque(false);

        for (int i = 0; i < TipoAlgoritmoVisual.values().length; i++) {
            PainelBarrasCanvas p = new PainelBarrasCanvas(true);
            JPanel cardArena = TemaVisual.criarCard(new BorderLayout());
            cardArena.add(p, BorderLayout.CENTER);
            paineis.add(p);
            grade.add(cardArena);
        }

        // 6ª célula: Placar / Pódio em tempo real
        JPanel painelPodio = TemaVisual.criarCard(new BorderLayout());

        JTable tabelaPodio = new JTable(modeloPodio);
        TemaVisual.estilizarTabela(tabelaPodio, 24);
        tabelaPodio.setFont(new Font(com.formdev.flatlaf.fonts.inter.FlatInterFont.FAMILY, Font.PLAIN, 11));
        tabelaPodio.getColumnModel().getColumn(0).setPreferredWidth(34);
        tabelaPodio.getColumnModel().getColumn(0).setMaxWidth(42);
        tabelaPodio.getColumnModel().getColumn(1).setPreferredWidth(135);
        tabelaPodio.getColumnModel().getColumn(2).setPreferredWidth(62);
        tabelaPodio.getColumnModel().getColumn(3).setPreferredWidth(60);
        tabelaPodio.getColumnModel().getColumn(4).setPreferredWidth(60);

        tabelaPodio.getColumnModel().getColumn(0).setCellRenderer(TemaVisual.criarRendererAlinhado(javax.swing.SwingConstants.CENTER));
        tabelaPodio.getColumnModel().getColumn(1).setCellRenderer(TemaVisual.criarRendererAlgoritmo());
        javax.swing.table.TableCellRenderer direita = TemaVisual.criarRendererAlinhado(javax.swing.SwingConstants.RIGHT);
        tabelaPodio.getColumnModel().getColumn(2).setCellRenderer(direita);
        tabelaPodio.getColumnModel().getColumn(3).setCellRenderer(direita);
        tabelaPodio.getColumnModel().getColumn(4).setCellRenderer(direita);

        JLabel nota = TemaVisual.criarRotuloSecundario(
                "<html><div style='padding:6px 10px;'>"
                        + "Inicie a corrida para acompanhar a ordem de chegada. Todos avançam o mesmo número de passos por quadro."
                        + "</div></html>");

        painelPodio.add(TemaVisual.criarCabecalhoCard("Placar da Corrida (Ordem de Chegada)"), BorderLayout.NORTH);
        painelPodio.add(TemaVisual.criarScrollLimpo(tabelaPodio), BorderLayout.CENTER);
        painelPodio.add(nota, BorderLayout.SOUTH);

        grade.add(painelPodio);
        return grade;
    }

    private void configurarEventos() {
        comboCenario.addActionListener(e -> recarregarCorrida());
        sliderQuantidade.addChangeListener(e -> {
            lblQuantidade.setText("Barras: " + sliderQuantidade.getValue());
            if (!sliderQuantidade.getValueIsAdjusting()) {
                recarregarCorrida();
            }
        });
        sliderVelocidade.addChangeListener(e -> {
            lblVelocidade.setText("Velocidade: " + sliderVelocidade.getValue() + "%");
            atualizarTimerDelay();
        });
        btnIniciarPausar.addActionListener(e -> alternarCorrida());
        btnReiniciar.addActionListener(e -> reiniciarMesmaSequencia());
        btnNovaSequencia.addActionListener(e -> {
            seedAtual = geradorSeeds.nextLong();
            recarregarCorrida();
        });
    }

    private void recarregarCorrida() {
        pausar();
        TipoCenario cenario = (TipoCenario) comboCenario.getSelectedItem();
        int qtd = sliderQuantidade.getValue();
        int[] baseComum = GeradorDados.gerarSequenciaVisual(qtd, cenario, seedAtual);

        reprodutores.clear();
        modeloPodio.setRowCount(0);
        proximoLugarPodio = 1;

        TipoAlgoritmoVisual[] tipos = TipoAlgoritmoVisual.values();
        for (int i = 0; i < tipos.length; i++) {
            ReprodutorAnimacao rep = new ReprodutorAnimacao(tipos[i], baseComum);
            reprodutores.add(rep);
            paineis.get(i).setReprodutor(rep);
        }
    }

    private void reiniciarMesmaSequencia() {
        pausar();
        modeloPodio.setRowCount(0);
        proximoLugarPodio = 1;
        for (int i = 0; i < reprodutores.size(); i++) {
            reprodutores.get(i).reiniciar();
            paineis.get(i).setPosicaoPodio(0);
            paineis.get(i).repaint();
        }
    }

    private void alternarCorrida() {
        if (timer.isRunning()) {
            pausar();
        } else {
            boolean todosConcluidos = reprodutores.stream().allMatch(ReprodutorAnimacao::isConcluido);
            if (todosConcluidos) {
                reiniciarMesmaSequencia();
            }
            atualizarTimerDelay();
            timer.start();
            btnIniciarPausar.setText("⏸  Pausar");
        }
    }

    public void pausar() {
        if (timer.isRunning()) {
            timer.stop();
        }
        btnIniciarPausar.setText("▶  Iniciar Corrida");
    }

    private void atualizarTimerDelay() {
        int vel = sliderVelocidade.getValue();
        int delay = vel <= 45
                ? (int) Math.round(180 - ((vel - 1) * (160.0 / 44.0)))
                : 16;
        timer.setDelay(Math.max(16, delay));
    }

    private int calcularPassosPorTick() {
        int vel = sliderVelocidade.getValue();
        if (vel <= 45) return 1;
        return 1 + ((vel - 45) / 2);
    }

    private void aoTickCorrida() {
        int passosPorTick = calcularPassosPorTick();
        boolean algumAtivo = false;

        for (int i = 0; i < reprodutores.size(); i++) {
            ReprodutorAnimacao rep = reprodutores.get(i);
            if (!rep.isConcluido()) {
                rep.avancarPassos(passosPorTick);
                if (rep.isConcluido()) {
                    int pos = proximoLugarPodio++;
                    paineis.get(i).setPosicaoPodio(pos);
                    modeloPodio.addRow(new Object[]{
                            pos + "º",
                            rep.getAlgoritmo().getNome(),
                            String.format("%,d", rep.getTotalPassos()),
                            String.format("%,d", rep.getTotalComparacoes()),
                            String.format("%,d", rep.getTotalMovimentacoes())
                    });
                } else {
                    algumAtivo = true;
                }
                paineis.get(i).repaint();
            }
        }

        if (!algumAtivo) {
            pausar();
        }
    }
}
