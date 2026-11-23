package com.sortanalyzer.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;

/**
 * Centraliza a identidade visual Teal + Slate do Java Sort Analyzer,
 * tipografia (Inter), alternância de temas Claro/Escuro via FlatLaf,
 * cores dos algoritmos e fábricas de componentes/renderizadores reutilizáveis.
 */
public final class TemaVisual {

    // Escala Teal (identidade principal)
    public static final Color VERDE_CABECALHO = new Color(15, 76, 71);        // #0F4C47
    public static final Color VERDE_AMAZONIA_ESCURO = new Color(17, 94, 89);  // #115E59
    public static final Color VERDE_AMAZONIA = new Color(15, 118, 110);       // #0F766E
    public static final Color VERDE_HOVER = new Color(13, 148, 136);          // #0D9488
    public static final Color VERDE_AMAZONIA_CLARO = new Color(20, 184, 166); // #14B8A6
    public static final Color VERDE_DARK_DESTAQUE = new Color(45, 212, 191);  // #2DD4BF
    public static final Color VERDE_SUBTITULO = new Color(204, 251, 241);     // #CCFBF1

    // Cores semânticas e de apoio
    public static final Color AZUL_SATELITE = new Color(37, 99, 235);
    public static final Color VERMELHO_CANCELAR = new Color(220, 38, 38);

    // Cores fixas dos 5 algoritmos (compartilhadas entre tabelas e gráficos)
    public static final Color COR_BUBBLE = new Color(239, 68, 68);     // Vermelho
    public static final Color COR_INSERTION = new Color(245, 158, 11); // Laranja
    public static final Color COR_MERGE = new Color(16, 185, 129);     // Verde
    public static final Color COR_QUICK = new Color(59, 130, 246);     // Azul
    public static final Color COR_HEAP = new Color(139, 92, 246);      // Violeta

    // Cores semânticas do Visualizador Animado
    public static final Color ANIM_NORMAL = VERDE_AMAZONIA;            // #0F766E
    public static final Color ANIM_NORMAL_DARK = VERDE_AMAZONIA_CLARO; // #14B8A6
    public static final Color ANIM_COMPARANDO = new Color(217, 119, 6);// #D97706
    public static final Color ANIM_TROCANDO = new Color(220, 98, 98);  // #DC6262
    public static final Color ANIM_PIVO = COR_HEAP;                    // #8B5CF6
    public static final Color ANIM_ORDENADO = new Color(22, 163, 74);  // #16A34A

    // Escala Slate — Tema Claro
    public static final Color FUNDO_JANELA = new Color(241, 245, 249);      // #F1F5F9
    public static final Color FUNDO_CARD = Color.WHITE;                     // #FFFFFF
    public static final Color FUNDO_SUTIL = new Color(248, 250, 252);       // #F8FAFC
    public static final Color BORDA_CLARA = new Color(226, 232, 240);       // #E2E8F0
    public static final Color BORDA_SUAVE = new Color(203, 213, 225);       // #CBD5E1
    public static final Color TEXTO_PRINCIPAL = new Color(23, 32, 51);      // #172033
    public static final Color TEXTO_SECUNDARIO = new Color(100, 116, 139);  // #64748B

    // Escala Slate — Tema Escuro
    public static final Color DARK_FUNDO_JANELA = new Color(15, 23, 42);    // #0F172A
    public static final Color DARK_FUNDO_CARD = new Color(30, 41, 59);      // #1E293B
    public static final Color DARK_FUNDO_SUTIL = new Color(24, 34, 52);     // #182234
    public static final Color DARK_BORDA_SUAVE = new Color(51, 65, 85);     // #334155
    public static final Color DARK_TEXTO_PRINCIPAL = new Color(241, 245, 249); // #F1F5F9
    public static final Color DARK_TEXTO_SECUNDARIO = new Color(148, 163, 184); // #94A3B8

    public static final Font FONTE_TITULO = new Font(FlatInterFont.FAMILY, Font.BOLD, 16);
    public static final Font FONTE_SUBTITULO = new Font(FlatInterFont.FAMILY, Font.BOLD, 13);
    public static final Font FONTE_NORMAL = new Font(FlatInterFont.FAMILY, Font.PLAIN, 12);

    private static boolean modoEscuro = false;
    private static final List<Runnable> listenersTema = new ArrayList<>();

    private TemaVisual() {
    }

    public static boolean isModoEscuro() {
        return modoEscuro;
    }

    public static void registrarListenerTema(Runnable listener) {
        if (listener != null) {
            listenersTema.add(listener);
        }
    }

    public static Color corFundoJanela() {
        return modoEscuro ? DARK_FUNDO_JANELA : FUNDO_JANELA;
    }

    public static Color corFundoCard() {
        return modoEscuro ? DARK_FUNDO_CARD : FUNDO_CARD;
    }

    public static Color corFundoSutil() {
        return modoEscuro ? DARK_FUNDO_SUTIL : FUNDO_SUTIL;
    }

    public static Color corBordaSuave() {
        return modoEscuro ? DARK_BORDA_SUAVE : BORDA_CLARA;
    }

    public static Color corTextoPrincipal() {
        return modoEscuro ? DARK_TEXTO_PRINCIPAL : TEXTO_PRINCIPAL;
    }

    public static Color corTextoSecundario() {
        return modoEscuro ? DARK_TEXTO_SECUNDARIO : TEXTO_SECUNDARIO;
    }

    public static Color corDestaqueTexto() {
        return modoEscuro ? VERDE_DARK_DESTAQUE : VERDE_AMAZONIA;
    }

    public static Color corLinhaGrade() {
        return modoEscuro ? DARK_BORDA_SUAVE : BORDA_CLARA;
    }

    /**
     * Retorna a cor canônica do algoritmo para manter consistência entre tabelas e gráficos.
     */
    public static Color corAlgoritmo(String nomeAlgoritmo) {
        if (nomeAlgoritmo == null) {
            return VERDE_AMAZONIA;
        }
        String n = nomeAlgoritmo.toLowerCase(Locale.ROOT);
        if (n.contains("bubble")) return COR_BUBBLE;
        if (n.contains("insertion")) return COR_INSERTION;
        if (n.contains("merge")) return COR_MERGE;
        if (n.contains("quick")) return COR_QUICK;
        if (n.contains("heap")) return COR_HEAP;
        return VERDE_AMAZONIA;
    }

    public static void aplicarLookAndFeel() {
        aplicarLookAndFeel(false);
    }

    public static void aplicarLookAndFeel(boolean escuro) {
        modoEscuro = escuro;
        try {
            FlatInterFont.install();
            FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);
            FlatLaf.setPreferredLightFontFamily(FlatInterFont.FAMILY_LIGHT);
            FlatLaf.setPreferredSemiboldFontFamily(FlatInterFont.FAMILY_SEMIBOLD);

            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap(
                    "@accentColor", escuro ? "#2DD4BF" : "#0F766E"
            ));

            if (escuro) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            configurarPropriedadesFlatLaf(escuro);
            FlatLaf.updateUI();

            for (Runnable r : listenersTema) {
                r.run();
            }
        } catch (Exception ignored) {
            // Mantém Look & Feel padrão do sistema caso ocorra falha inesperada
        }
    }

    public static void alternarTema() {
        aplicarLookAndFeel(!modoEscuro);
    }

    private static void configurarPropriedadesFlatLaf(boolean escuro) {
        UIManager.put("defaultFont", new FontUIResource(FlatInterFont.FAMILY, Font.PLAIN, 12));

        // Arredondamento e foco padronizados
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("CheckBox.arc", 4);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.borderColor", uiColor(escuro ? DARK_BORDA_SUAVE : BORDA_SUAVE));
        UIManager.put("Component.disabledBorderColor", uiColor(escuro ? new Color(36, 48, 68) : BORDA_CLARA));
        UIManager.put("Component.focusColor", uiColor(escuro ? new Color(45, 212, 191, 140) : new Color(15, 118, 110, 120)));

        // Campos de entrada e seletores alinhados à escala Slate
        Color bgInput = escuro ? DARK_FUNDO_JANELA : FUNDO_CARD;
        Color fgInput = escuro ? DARK_TEXTO_PRINCIPAL : TEXTO_PRINCIPAL;
        UIManager.put("TextField.background", uiColor(bgInput));
        UIManager.put("TextField.foreground", uiColor(fgInput));
        UIManager.put("FormattedTextField.background", uiColor(bgInput));
        UIManager.put("FormattedTextField.foreground", uiColor(fgInput));
        UIManager.put("TextArea.background", uiColor(bgInput));
        UIManager.put("TextArea.foreground", uiColor(fgInput));
        UIManager.put("ComboBox.background", uiColor(bgInput));
        UIManager.put("ComboBox.foreground", uiColor(fgInput));
        UIManager.put("ComboBox.buttonBackground", uiColor(bgInput));
        UIManager.put("ComboBox.selectionBackground", uiColor(escuro ? VERDE_AMAZONIA : VERDE_AMAZONIA));
        UIManager.put("ComboBox.selectionForeground", uiColor(Color.WHITE));
        UIManager.put("Spinner.background", uiColor(bgInput));
        UIManager.put("Spinner.foreground", uiColor(fgInput));
        UIManager.put("Spinner.buttonBackground", uiColor(escuro ? DARK_FUNDO_SUTIL : FUNDO_SUTIL));

        // Sliders e CheckBoxes em Teal
        UIManager.put("Slider.thumbColor", uiColor(escuro ? VERDE_DARK_DESTAQUE : VERDE_AMAZONIA));
        UIManager.put("Slider.hoverThumbColor", uiColor(escuro ? VERDE_AMAZONIA_CLARO : VERDE_HOVER));
        UIManager.put("Slider.trackValueColor", uiColor(escuro ? VERDE_AMAZONIA_CLARO : VERDE_AMAZONIA));
        UIManager.put("Slider.trackColor", uiColor(escuro ? DARK_BORDA_SUAVE : BORDA_SUAVE));

        // Barra de progresso em Teal
        UIManager.put("ProgressBar.foreground", uiColor(escuro ? VERDE_AMAZONIA_CLARO : VERDE_AMAZONIA));
        UIManager.put("ProgressBar.background", uiColor(escuro ? DARK_BORDA_SUAVE : BORDA_CLARA));
        UIManager.put("ProgressBar.selectionForeground", uiColor(Color.WHITE));
        UIManager.put("ProgressBar.selectionBackground", uiColor(escuro ? DARK_TEXTO_PRINCIPAL : TEXTO_PRINCIPAL));

        // Abas principais com indicador inferior em Teal
        UIManager.put("TabbedPane.tabHeight", 38);
        UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(7, 18, 7, 18));
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.background", uiColor(escuro ? DARK_FUNDO_JANELA : FUNDO_JANELA));
        UIManager.put("TabbedPane.selectedBackground", uiColor(escuro ? DARK_FUNDO_CARD : FUNDO_CARD));
        UIManager.put("TabbedPane.selectedForeground", uiColor(escuro ? VERDE_DARK_DESTAQUE : VERDE_AMAZONIA_ESCURO));
        UIManager.put("TabbedPane.foreground", uiColor(escuro ? DARK_TEXTO_SECUNDARIO : TEXTO_SECUNDARIO));
        UIManager.put("TabbedPane.hoverColor", uiColor(escuro ? DARK_FUNDO_SUTIL : BORDA_CLARA));
        UIManager.put("TabbedPane.underlineColor", uiColor(escuro ? VERDE_DARK_DESTAQUE : VERDE_AMAZONIA));
        UIManager.put("TabbedPane.inactiveUnderlineColor", uiColor(escuro ? VERDE_AMAZONIA : new Color(15, 118, 110, 140)));
        UIManager.put("TabbedPane.tabSelectionHeight", 3);
        UIManager.put("TabbedPane.contentSeparatorHeight", 1);

        // Scrollbars finas
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new InsetsUIResource(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 10);

        // Tabelas: cabeçalhos legíveis, linhas alternadas discretas, bordas verticais suaves
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new DimensionUIResource(0, 1));
        UIManager.put("Table.cellMargins", new InsetsUIResource(4, 10, 4, 10));
        UIManager.put("TableHeader.height", 30);
        UIManager.put("TableHeader.bottomSeparatorColor", uiColor(escuro ? DARK_BORDA_SUAVE : BORDA_CLARA));
        UIManager.put("TableHeader.separatorColor", uiColor(escuro ? new Color(51, 65, 85, 90) : new Color(226, 232, 240, 140)));
        UIManager.put("TableHeader.background", uiColor(escuro ? DARK_FUNDO_SUTIL : FUNDO_SUTIL));
        UIManager.put("TableHeader.foreground", uiColor(escuro ? DARK_TEXTO_SECUNDARIO : TEXTO_SECUNDARIO));
        UIManager.put("Table.background", uiColor(escuro ? DARK_FUNDO_CARD : FUNDO_CARD));
        UIManager.put("Table.foreground", uiColor(escuro ? DARK_TEXTO_PRINCIPAL : TEXTO_PRINCIPAL));
        UIManager.put("Table.alternateRowColor", uiColor(escuro ? new Color(36, 48, 68) : FUNDO_SUTIL));
        UIManager.put("Table.gridColor", uiColor(escuro ? new Color(51, 65, 85, 150) : BORDA_CLARA));
        UIManager.put("Table.selectionBackground", uiColor(escuro ? new Color(17, 94, 89) : new Color(204, 251, 241)));
        UIManager.put("Table.selectionForeground", uiColor(escuro ? DARK_TEXTO_PRINCIPAL : TEXTO_PRINCIPAL));
        UIManager.put("Table.selectionInactiveBackground", uiColor(escuro ? new Color(30, 58, 68) : new Color(240, 253, 250)));

        // Painéis e divisores
        UIManager.put("Panel.background", uiColor(escuro ? DARK_FUNDO_JANELA : FUNDO_JANELA));
        UIManager.put("Viewport.background", uiColor(escuro ? DARK_FUNDO_CARD : FUNDO_CARD));
        UIManager.put("SplitPane.dividerSize", 6);
        UIManager.put("SplitPaneDivider.gripDotCount", 3);
    }

    private static ColorUIResource uiColor(Color c) {
        return new ColorUIResource(c);
    }

    public static JButton criarBotaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; "
                        + "margin: 5,14,5,14; "
                        + "borderWidth: 0; "
                        + "focusWidth: 1; "
                        + "[light]background: #0F766E; [light]hoverBackground: #0D9488; [light]pressedBackground: #115E59; [light]foreground: #FFFFFF; "
                        + "[dark]background: #0D9488; [dark]hoverBackground: #14B8A6; [dark]pressedBackground: #0F766E; [dark]foreground: #FFFFFF; "
                        + "[light]disabledBackground: #CBD5E1; [light]disabledText: #64748B; "
                        + "[dark]disabledBackground: #334155; [dark]disabledText: #64748B");
        return btn;
    }

    public static JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; "
                        + "margin: 5,12,5,12; "
                        + "borderWidth: 1; "
                        + "[light]background: #F8FAFC; [light]hoverBackground: #E2E8F0; [light]pressedBackground: #CBD5E1; "
                        + "[light]borderColor: #CBD5E1; [light]foreground: #0F172A; "
                        + "[dark]background: #1E293B; [dark]hoverBackground: #334155; [dark]pressedBackground: #475569; "
                        + "[dark]borderColor: #334155; [dark]foreground: #F1F5F9; "
                        + "[light]disabledBackground: #F1F5F9; [light]disabledText: #94A3B8; "
                        + "[dark]disabledBackground: #182234; [dark]disabledText: #475569");
        return btn;
    }

    public static JButton criarBotaoPerigo(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; "
                        + "margin: 5,12,5,12; "
                        + "borderWidth: 0; "
                        + "[light]background: #DC2626; [light]hoverBackground: #EF4444; [light]pressedBackground: #B91C1C; [light]foreground: #FFFFFF; "
                        + "[dark]background: #DC2626; [dark]hoverBackground: #EF4444; [dark]pressedBackground: #B91C1C; [dark]foreground: #FFFFFF; "
                        + "[light]disabledBackground: #F1F5F9; [light]disabledText: #94A3B8; "
                        + "[dark]disabledBackground: #182234; [dark]disabledText: #475569");
        return btn;
    }

    public static JButton criarBotaoCabecalho(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 8; "
                        + "margin: 5,12,5,12; "
                        + "borderWidth: 1; "
                        + "background: #115E59; "
                        + "hoverBackground: #0F766E; "
                        + "pressedBackground: #0F4C47; "
                        + "borderColor: #14B8A6; "
                        + "foreground: #CCFBF1");
        return btn;
    }

    public static JPanel criarCard(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; "
                        + "[light]background: #FFFFFF; [light]border: 1,1,1,1,#E2E8F0,1,10; "
                        + "[dark]background: #1E293B; [dark]border: 1,1,1,1,#334155,1,10");
        return card;
    }

    public static JPanel criarCabecalhoCard(String titulo) {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.putClientProperty(FlatClientProperties.STYLE,
                "[light]background: #F8FAFC; [dark]background: #182234");
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(148, 163, 184, 65)),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        JPanel faixa = new JPanel();
        faixa.putClientProperty(FlatClientProperties.STYLE,
                "arc: 4; [light]background: #0F766E; [dark]background: #2DD4BF");
        faixa.setPreferredSize(new Dimension(3, 15));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
        lbl.putClientProperty(FlatClientProperties.STYLE,
                "[light]foreground: #0F172A; [dark]foreground: #F1F5F9");

        header.add(faixa, BorderLayout.WEST);
        header.add(lbl, BorderLayout.CENTER);
        return header;
    }

    /**
     * Cria um seletor segmentado (pill toggle) para substituir abas aninhadas.
     */
    public static JPanel criarBarraSegmentada(String[] opcoes, int selecionadoInicial, IntConsumer aoSelecionar) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        wrap.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; "
                        + "[light]background: #E2E8F0; "
                        + "[dark]background: #0F172A");

        ButtonGroup grupo = new ButtonGroup();
        for (int i = 0; i < opcoes.length; i++) {
            final int idx = i;
            JToggleButton tb = new JToggleButton(opcoes[i], i == selecionadoInicial);
            tb.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tb.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 8; "
                            + "margin: 5,14,5,14; "
                            + "borderWidth: 0; "
                            + "focusWidth: 0; "
                            + "[light]background: #E2E8F0; [light]hoverBackground: #CBD5E1; [light]foreground: #475569; "
                            + "[light]selectedBackground: #FFFFFF; [light]selectedForeground: #0F766E; "
                            + "[dark]background: #0F172A; [dark]hoverBackground: #1E293B; [dark]foreground: #94A3B8; "
                            + "[dark]selectedBackground: #1E293B; [dark]selectedForeground: #2DD4BF");
            tb.addActionListener(e -> aoSelecionar.accept(idx));
            grupo.add(tb);
            wrap.add(tb);
        }
        return wrap;
    }

    public static void estilizarTabela(JTable tabela, int rowHeight) {
        tabela.setRowHeight(rowHeight);
        tabela.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 12));
        tabela.getTableHeader().setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 11));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setFillsViewportHeight(true);
    }

    public static JScrollPane criarScrollLimpo(JComponent componente) {
        JScrollPane sp = new JScrollPane(componente);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    public static JLabel criarRotuloSecundario(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(FlatInterFont.FAMILY, Font.PLAIN, 11));
        lbl.putClientProperty(FlatClientProperties.STYLE,
                "[light]foreground: #475569; [dark]foreground: #94A3B8");
        return lbl;
    }

    public static JLabel criarRotuloStatus(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 12));
        lbl.putClientProperty(FlatClientProperties.STYLE,
                "[light]foreground: #115E59; [dark]foreground: #2DD4BF");
        return lbl;
    }

    public static JLabel criarEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.LEFT);
        lbl.setFont(new Font(FlatInterFont.FAMILY, Font.BOLD, 11));
        lbl.putClientProperty(FlatClientProperties.STYLE,
                "[light]foreground: #475569; [dark]foreground: #94A3B8");
        return lbl;
    }

    /**
     * Renderizador alinhado que respeita dinamicamente o tema claro/escuro.
     */
    public static TableCellRenderer criarRendererAlinhado(int horizontalAlignment) {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(horizontalAlignment);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setForeground(isSelected ? table.getSelectionForeground() : corTextoPrincipal());
                return this;
            }
        };
    }

    /**
     * Renderizador de nome de algoritmo com indicador circular na mesma cor do gráfico.
     */
    public static TableCellRenderer criarRendererAlgoritmo() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String nome = value != null ? value.toString() : "";
                setIcon(new IconePontoCor(corAlgoritmo(nome), 8));
                setIconTextGap(7);
                setForeground(isSelected ? table.getSelectionForeground() : corTextoPrincipal());
                return this;
            }
        };
    }

    /**
     * Renderizador da coluna Validação ("OK" em Teal discreto, "FALHA" em vermelho).
     */
    public static TableCellRenderer criarRendererValidacao() {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String txt = value != null ? value.toString() : "";
                setFont(table.getFont().deriveFont(Font.BOLD));
                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                } else if ("OK".equalsIgnoreCase(txt)) {
                    setForeground(modoEscuro ? VERDE_DARK_DESTAQUE : VERDE_AMAZONIA);
                } else {
                    setForeground(VERMELHO_CANCELAR);
                }
                return this;
            }
        };
    }

    /**
     * Renderizador da coluna Tempo (ms) com destaque discreto no melhor tempo.
     */
    public static TableCellRenderer criarRendererTempo(DoubleSupplier menorTempoSupplier) {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String txt = value != null ? value.toString() : "";
                boolean melhor = false;
                if (menorTempoSupplier != null && table.getRowCount() > 1) {
                    try {
                        double val = Double.parseDouble(txt.replace(",", "."));
                        double min = menorTempoSupplier.getAsDouble();
                        if (min < Double.MAX_VALUE && Math.abs(val - min) < 0.0005) {
                            melhor = true;
                        }
                    } catch (NumberFormatException ignored) {
                        // Mantém formatação padrão
                    }
                }
                if (melhor) {
                    setText("★ " + txt);
                    setFont(table.getFont().deriveFont(Font.BOLD));
                    setForeground(isSelected ? table.getSelectionForeground() : corDestaqueTexto());
                } else {
                    setText(txt);
                    setFont(table.getFont().deriveFont(Font.PLAIN));
                    setForeground(isSelected ? table.getSelectionForeground() : corTextoPrincipal());
                }
                return this;
            }
        };
    }

    private static final class IconePontoCor implements Icon {
        private final Color cor;
        private final int tamanho;

        private IconePontoCor(Color cor, int tamanho) {
            this.cor = cor;
            this.tamanho = tamanho;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cor);
                g2.fillOval(x, y, tamanho, tamanho);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return tamanho;
        }

        @Override
        public int getIconHeight() {
            return tamanho;
        }
    }
}
