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
import javax.swing.JSlider;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Random;

/**
 * Modo Individual do Visualizador Animado: permite acompanhar passo a passo
 * o funcionamento interno de um algoritmo escolhido, com controle de velocidade,
 * avanço/retrocesso manual e descrição textual de cada operação.
 */
public final class PainelModoIndividual extends JPanel {

    private final JComboBox<TipoAlgoritmoVisual> comboAlgoritmo;
    private final JComboBox<TipoCenario> comboCenario;
    private final JSlider sliderQuantidade;
    private final JLabel lblQuantidade;
    private final JSlider sliderVelocidade;
    private final JLabel lblVelocidade;

    private final JButton btnPlayPause;
    private final JButton btnVoltar;
    private final JButton btnAvancar;
    private final JButton btnReiniciar;
    private final JButton btnNovaSequencia;

    private final PainelBarrasCanvas canvas;
    private final JLabel lblDescricaoPasso;

    private final Timer timer;
    private final Random geradorSeeds = new Random();
    private long seedAtual = GeradorDados.SEED_PADRAO;
    private ReprodutorAnimacao reprodutor;

    public PainelModoIndividual() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);

        comboAlgoritmo = new JComboBox<>(TipoAlgoritmoVisual.values());
        comboCenario = new JComboBox<>(TipoCenario.cenariosGeracao());

        sliderQuantidade = new JSlider(10, 150, 35);
        sliderQuantidade.setOpaque(false);
        lblQuantidade = new JLabel("Barras: 35");

        sliderVelocidade = new JSlider(1, 100, 45);
        sliderVelocidade.setOpaque(false);
        lblVelocidade = new JLabel("Velocidade: 45%");

        btnPlayPause = TemaVisual.criarBotaoPrimario("▶  Iniciar");
        btnVoltar = TemaVisual.criarBotaoSecundario("‹ Passo");
        btnAvancar = TemaVisual.criarBotaoSecundario("Passo ›");
        btnReiniciar = TemaVisual.criarBotaoSecundario("Reiniciar");
        btnNovaSequencia = TemaVisual.criarBotaoSecundario("Nova Sequência");

        canvas = new PainelBarrasCanvas(false);
        lblDescricaoPasso = new JLabel(" ");
        lblDescricaoPasso.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lblDescricaoPasso.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 8; "
                        + "[light]background: #FFFFFF; [light]foreground: #0F172A; [light]border: 8,12,8,12,#E2E8F0,1,8; "
                        + "[dark]background: #1E293B; [dark]foreground: #F1F5F9; [dark]border: 8,12,8,12,#334155,1,8");
        lblDescricaoPasso.setOpaque(true);

        JPanel cardCanvas = TemaVisual.criarCard(new BorderLayout());
        cardCanvas.add(canvas, BorderLayout.CENTER);

        add(criarBarraControles(), BorderLayout.NORTH);
        add(cardCanvas, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        timer = new Timer(40, e -> aoTickTimer());

        configurarEventos();
        recarregarAnimacao();
    }

    private JPanel criarBarraControles() {
        JPanel card = TemaVisual.criarCard(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        sliderQuantidade.setPreferredSize(new java.awt.Dimension(130, 24));
        sliderVelocidade.setPreferredSize(new java.awt.Dimension(130, 24));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linha1.setOpaque(false);
        linha1.add(TemaVisual.criarEtiquetaCampo("ALGORITMO:"));
        linha1.add(comboAlgoritmo);
        linha1.add(TemaVisual.criarEtiquetaCampo("CENÁRIO:"));
        linha1.add(comboCenario);
        linha1.add(lblQuantidade);
        linha1.add(sliderQuantidade);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha2.setOpaque(false);
        linha2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(148, 163, 184, 70)),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)
        ));
        linha2.add(btnPlayPause);
        linha2.add(btnVoltar);
        linha2.add(btnAvancar);
        linha2.add(btnReiniciar);
        linha2.add(btnNovaSequencia);
        linha2.add(new JLabel("   "));
        linha2.add(lblVelocidade);
        linha2.add(sliderVelocidade);

        card.add(linha1, BorderLayout.NORTH);
        card.add(linha2, BorderLayout.SOUTH);
        return card;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout(6, 6));
        rodape.setOpaque(false);
        rodape.add(lblDescricaoPasso, BorderLayout.NORTH);
        rodape.add(criarLegendaCores(), BorderLayout.SOUTH);
        return rodape;
    }

    public static JPanel criarLegendaCores() {
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
        legenda.setOpaque(false);
        legenda.add(itemLegenda(PainelBarrasCanvas.COR_BARRA_NORMAL, "Normal"));
        legenda.add(itemLegenda(PainelBarrasCanvas.COR_COMPARANDO, "Comparando"));
        legenda.add(itemLegenda(PainelBarrasCanvas.COR_TROCANDO, "Trocando / Escrevendo"));
        legenda.add(itemLegenda(PainelBarrasCanvas.COR_PIVO, "Pivô / Chave"));
        legenda.add(itemLegenda(PainelBarrasCanvas.COR_ORDENADO, "Ordenado"));
        return legenda;
    }

    private static JPanel itemLegenda(Color cor, String texto) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        JPanel quadrado = new JPanel();
        quadrado.setBackground(cor);
        quadrado.setPreferredSize(new java.awt.Dimension(12, 12));
        quadrado.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "arc: 4");
        item.add(quadrado);
        JLabel lbl = TemaVisual.criarRotuloSecundario(texto);
        item.add(lbl);
        return item;
    }

    private void configurarEventos() {
        comboAlgoritmo.addActionListener(e -> recarregarAnimacao());
        comboCenario.addActionListener(e -> recarregarAnimacao());

        sliderQuantidade.addChangeListener(e -> {
            lblQuantidade.setText("Barras: " + sliderQuantidade.getValue());
            if (!sliderQuantidade.getValueIsAdjusting()) {
                recarregarAnimacao();
            }
        });

        sliderVelocidade.addChangeListener(e -> {
            lblVelocidade.setText("Velocidade: " + sliderVelocidade.getValue() + "%");
            atualizarTimerDelay();
        });

        btnPlayPause.addActionListener(e -> alternarExecucao());
        btnAvancar.addActionListener(e -> {
            pausar();
            reprodutor.avancarUmPasso();
            atualizarTela();
        });
        btnVoltar.addActionListener(e -> {
            pausar();
            reprodutor.voltarUmPasso();
            atualizarTela();
        });
        btnReiniciar.addActionListener(e -> {
            pausar();
            reprodutor.reiniciar();
            atualizarTela();
        });
        btnNovaSequencia.addActionListener(e -> {
            seedAtual = geradorSeeds.nextLong();
            recarregarAnimacao();
        });
    }

    private void recarregarAnimacao() {
        pausar();
        TipoAlgoritmoVisual alg = (TipoAlgoritmoVisual) comboAlgoritmo.getSelectedItem();
        TipoCenario cen = (TipoCenario) comboCenario.getSelectedItem();
        int qtd = sliderQuantidade.getValue();
        int[] seq = GeradorDados.gerarSequenciaVisual(qtd, cen, seedAtual);

        reprodutor = new ReprodutorAnimacao(alg, seq);
        canvas.setReprodutor(reprodutor);
        atualizarTela();
    }

    private void alternarExecucao() {
        if (timer.isRunning()) {
            pausar();
        } else {
            if (reprodutor.isConcluido()) {
                reprodutor.reiniciar();
            }
            atualizarTimerDelay();
            timer.start();
            btnPlayPause.setText("⏸  Pausar");
        }
    }

    public void pausar() {
        if (timer.isRunning()) {
            timer.stop();
        }
        btnPlayPause.setText("▶  Iniciar");
    }

    private void atualizarTimerDelay() {
        int vel = sliderVelocidade.getValue();
        int delay = vel <= 55
                ? (int) Math.round(320 - ((vel - 1) * (300.0 / 54.0)))
                : 16;
        timer.setDelay(Math.max(16, delay));
    }

    private int calcularPassosPorTick() {
        int vel = sliderVelocidade.getValue();
        if (vel <= 55) return 1;
        return 1 + ((vel - 55) / 2);
    }

    private void aoTickTimer() {
        reprodutor.avancarPassos(calcularPassosPorTick());
        atualizarTela();
        if (reprodutor.isConcluido()) {
            pausar();
        }
    }

    private void atualizarTela() {
        canvas.repaint();
        lblDescricaoPasso.setText("Operação atual: " + reprodutor.getDescricaoAtual());
        btnVoltar.setEnabled(reprodutor.getIndicePasso() > 0);
        btnAvancar.setEnabled(!reprodutor.isConcluido());
    }
}
