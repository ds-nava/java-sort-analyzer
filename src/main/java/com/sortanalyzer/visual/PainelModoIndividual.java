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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(TemaVisual.FUNDO_JANELA);

        comboAlgoritmo = new JComboBox<>(TipoAlgoritmoVisual.values());
        comboCenario = new JComboBox<>(TipoCenario.cenariosGeracao());

        sliderQuantidade = new JSlider(10, 150, 35);
        sliderQuantidade.setOpaque(false);
        lblQuantidade = new JLabel("Barras: 35");

        sliderVelocidade = new JSlider(1, 100, 45);
        sliderVelocidade.setOpaque(false);
        lblVelocidade = new JLabel("Velocidade: 45%");

        btnPlayPause = TemaVisual.criarBotaoPrimario("Iniciar");
        btnVoltar = TemaVisual.criarBotaoSecundario("< Passo");
        btnAvancar = TemaVisual.criarBotaoSecundario("Passo >");
        btnReiniciar = TemaVisual.criarBotaoSecundario("Reiniciar");
        btnNovaSequencia = TemaVisual.criarBotaoSecundario("Nova Sequência");

        canvas = new PainelBarrasCanvas(false);
        lblDescricaoPasso = new JLabel(" ");
        lblDescricaoPasso.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lblDescricaoPasso.setForeground(TemaVisual.TEXTO_PRINCIPAL);
        lblDescricaoPasso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.BORDA_SUAVE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        lblDescricaoPasso.setOpaque(true);
        lblDescricaoPasso.setBackground(Color.WHITE);

        add(criarBarraControles(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        timer = new Timer(40, e -> aoTickTimer());

        configurarEventos();
        recarregarAnimacao();
    }

    private JPanel criarBarraControles() {
        JPanel container = new JPanel(new BorderLayout(6, 6));
        container.setOpaque(false);

        sliderQuantidade.setPreferredSize(new java.awt.Dimension(135, 26));
        sliderVelocidade.setPreferredSize(new java.awt.Dimension(135, 26));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        linha1.setBackground(Color.WHITE);
        linha1.setBorder(BorderFactory.createLineBorder(TemaVisual.BORDA_SUAVE));
        linha1.add(new JLabel("Algoritmo:"));
        linha1.add(comboAlgoritmo);
        linha1.add(new JLabel("Cenário:"));
        linha1.add(comboCenario);
        linha1.add(lblQuantidade);
        linha1.add(sliderQuantidade);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linha2.setOpaque(false);
        linha2.add(btnPlayPause);
        linha2.add(btnVoltar);
        linha2.add(btnAvancar);
        linha2.add(btnReiniciar);
        linha2.add(btnNovaSequencia);
        linha2.add(new JLabel("   "));
        linha2.add(lblVelocidade);
        linha2.add(sliderVelocidade);

        container.add(linha1, BorderLayout.NORTH);
        container.add(linha2, BorderLayout.SOUTH);
        return container;
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
        quadrado.setPreferredSize(new java.awt.Dimension(13, 13));
        quadrado.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        item.add(quadrado);
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
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
            btnPlayPause.setText("Pausar");
        }
    }

    public void pausar() {
        if (timer.isRunning()) {
            timer.stop();
        }
        btnPlayPause.setText("Iniciar");
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
