package com.sortanalyzer.ui;

import com.sortanalyzer.model.ImagemSatelite;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Painel reutilizável contendo título, tabela paginada de {@link ImagemSatelite}
 * e controles de navegação (Primeira / Anterior / Próxima / Última página).
 */
public final class PainelTabelaPaginada extends JPanel {

    private final TabelaCatalogoModel modelo;
    private final JLabel lblInfoPagina;
    private final JButton btnPrimeira;
    private final JButton btnAnterior;
    private final JButton btnProxima;
    private final JButton btnUltima;

    public PainelTabelaPaginada(String titulo) {
        setLayout(new BorderLayout(4, 4));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(TemaVisual.BORDA_SUAVE));

        JLabel lblTitulo = new JLabel("  " + titulo);
        lblTitulo.setOpaque(true);
        lblTitulo.setBackground(TemaVisual.VERDE_AMAZONIA_ESCURO);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        modelo = new TabelaCatalogoModel();
        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(22);
        tabela.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(75);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(70);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        rodape.setBackground(new Color(248, 250, 252));

        btnPrimeira = new JButton("<<");
        btnAnterior = new JButton("<");
        btnProxima = new JButton(">");
        btnUltima = new JButton(">>");
        lblInfoPagina = new JLabel("0 registros");
        lblInfoPagina.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        btnPrimeira.addActionListener(e -> irParaPagina(0));
        btnAnterior.addActionListener(e -> irParaPagina(modelo.getPaginaAtual() - 1));
        btnProxima.addActionListener(e -> irParaPagina(modelo.getPaginaAtual() + 1));
        btnUltima.addActionListener(e -> irParaPagina(modelo.getTotalPaginas() - 1));

        rodape.add(btnPrimeira);
        rodape.add(btnAnterior);
        rodape.add(lblInfoPagina);
        rodape.add(btnProxima);
        rodape.add(btnUltima);

        add(lblTitulo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        atualizarBotoes();
    }

    public void setDados(ImagemSatelite[] dados) {
        modelo.setDados(dados);
        atualizarBotoes();
    }

    private void irParaPagina(int pagina) {
        modelo.setPaginaAtual(pagina);
        atualizarBotoes();
    }

    private void atualizarBotoes() {
        int total = modelo.getTotalRegistros();
        int pagAtual = modelo.getPaginaAtual();
        int totalPags = modelo.getTotalPaginas();

        if (total == 0) {
            lblInfoPagina.setText("Nenhum dado carregado");
        } else {
            lblInfoPagina.setText(String.format("Página %,d / %,d  (%,d itens)", pagAtual + 1, totalPags, total));
        }

        btnPrimeira.setEnabled(total > 0 && pagAtual > 0);
        btnAnterior.setEnabled(total > 0 && pagAtual > 0);
        btnProxima.setEnabled(total > 0 && pagAtual < totalPags - 1);
        btnUltima.setEnabled(total > 0 && pagAtual < totalPags - 1);
    }
}
