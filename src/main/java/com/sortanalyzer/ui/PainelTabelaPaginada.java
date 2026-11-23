package com.sortanalyzer.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.sortanalyzer.model.ImagemSatelite;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
        setLayout(new BorderLayout(0, 0));
        putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; "
                        + "[light]background: #FFFFFF; [light]border: 1,1,1,1,#E2E8F0,1,10; "
                        + "[dark]background: #1E293B; [dark]border: 1,1,1,1,#334155,1,10");

        modelo = new TabelaCatalogoModel();
        JTable tabela = new JTable(modelo);
        TemaVisual.estilizarTabela(tabela, 23);
        tabela.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 11));
        tabela.getColumnModel().getColumn(0).setPreferredWidth(44);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(76);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(88);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(135);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(84);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(68);

        javax.swing.table.TableCellRenderer direita = TemaVisual.criarRendererAlinhado(SwingConstants.RIGHT);
        tabela.getColumnModel().getColumn(0).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(1).setCellRenderer(direita);
        tabela.getColumnModel().getColumn(5).setCellRenderer(direita);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        rodape.putClientProperty(FlatClientProperties.STYLE,
                "[light]background: #F8FAFC; [dark]background: #182234");
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(148, 163, 184, 70)));

        btnPrimeira = criarBotaoPaginacao("«");
        btnAnterior = criarBotaoPaginacao("‹");
        btnProxima = criarBotaoPaginacao("›");
        btnUltima = criarBotaoPaginacao("»");
        lblInfoPagina = TemaVisual.criarRotuloSecundario("Nenhum dado carregado");

        btnPrimeira.addActionListener(e -> irParaPagina(0));
        btnAnterior.addActionListener(e -> irParaPagina(modelo.getPaginaAtual() - 1));
        btnProxima.addActionListener(e -> irParaPagina(modelo.getPaginaAtual() + 1));
        btnUltima.addActionListener(e -> irParaPagina(modelo.getTotalPaginas() - 1));

        rodape.add(btnPrimeira);
        rodape.add(btnAnterior);
        rodape.add(lblInfoPagina);
        rodape.add(btnProxima);
        rodape.add(btnUltima);

        add(TemaVisual.criarCabecalhoCard(titulo), BorderLayout.NORTH);
        add(TemaVisual.criarScrollLimpo(tabela), BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        atualizarBotoes();
    }

    private static JButton criarBotaoPaginacao(String simbolo) {
        JButton b = new JButton(simbolo);
        b.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc: 6; margin: 2,8,2,8; borderWidth: 1; "
                        + "[light]background: #FFFFFF; [light]borderColor: #CBD5E1; [light]foreground: #334155; "
                        + "[dark]background: #1E293B; [dark]borderColor: #475569; [dark]foreground: #E2E8F0");
        return b;
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
            lblInfoPagina.setText("Execute a ordenação para visualizar");
        } else {
            lblInfoPagina.setText(String.format("Página %,d / %,d  (%,d itens)", pagAtual + 1, totalPags, total));
        }

        btnPrimeira.setEnabled(total > 0 && pagAtual > 0);
        btnAnterior.setEnabled(total > 0 && pagAtual > 0);
        btnProxima.setEnabled(total > 0 && pagAtual < totalPags - 1);
        btnUltima.setEnabled(total > 0 && pagAtual < totalPags - 1);
    }
}
