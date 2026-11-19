package com.sortanalyzer.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Padroniza paleta de cores (inspirada no monitoramento ambiental amazônico),
 * tipografia e botões da interface Swing.
 */
public final class TemaVisual {

    public static final Color VERDE_AMAZONIA = new Color(15, 118, 110);
    public static final Color VERDE_AMAZONIA_ESCURO = new Color(17, 94, 89);
    public static final Color AZUL_SATELITE = new Color(37, 99, 235);
    public static final Color FUNDO_JANELA = new Color(241, 245, 249);
    public static final Color FUNDO_CARD = Color.WHITE;
    public static final Color BORDA_SUAVE = new Color(203, 213, 225);
    public static final Color TEXTO_PRINCIPAL = new Color(15, 23, 42);
    public static final Color TEXTO_SECUNDARIO = new Color(71, 85, 105);
    public static final Color VERMELHO_CANCELAR = new Color(220, 38, 38);

    public static final Font FONTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FONTE_SUBTITULO = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONTE_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    private TemaVisual() {
    }

    public static void aplicarLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", FUNDO_JANELA);
            UIManager.put("nimbusBase", VERDE_AMAZONIA_ESCURO);
            UIManager.put("nimbusSelectionBackground", VERDE_AMAZONIA);
        } catch (Exception ignored) {
            // Mantém Look & Feel padrão do sistema caso Nimbus não esteja disponível
        }
    }

    public static JButton criarBotaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(VERDE_AMAZONIA);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    public static JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(226, 232, 240));
        btn.setForeground(TEXTO_PRINCIPAL);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        return btn;
    }

    public static JButton criarBotaoPerigo(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(VERMELHO_CANCELAR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }
}
