package com.sortanalyzer.ui;

import com.sortanalyzer.model.ImagemSatelite;

import javax.swing.table.AbstractTableModel;
import java.util.Locale;

/**
 * Modelo de tabela paginado baseado em {@link AbstractTableModel}.
 * Permite inspecionar bases com 100.000+ registros sem sobrecarga visual na EDT do Swing.
 */
public final class TabelaCatalogoModel extends AbstractTableModel {

    private static final String[] COLUNAS = {
            "#", "ID Imagem", "Satélite", "Data/Hora (UTC)", "Região", "Índice (%)"
    };

    private ImagemSatelite[] dados = new ImagemSatelite[0];
    private int tamanhoPagina = 100;
    private int paginaAtual = 0;

    public void setDados(ImagemSatelite[] dados) {
        this.dados = dados != null ? dados : new ImagemSatelite[0];
        this.paginaAtual = 0;
        fireTableDataChanged();
    }

    public int getTotalRegistros() {
        return dados.length;
    }

    public int getTotalPaginas() {
        if (dados.length == 0) return 1;
        return (int) Math.ceil((double) dados.length / tamanhoPagina);
    }

    public int getPaginaAtual() {
        return paginaAtual;
    }

    public void setPaginaAtual(int pagina) {
        int max = getTotalPaginas() - 1;
        this.paginaAtual = Math.max(0, Math.min(pagina, max));
        fireTableDataChanged();
    }

    public void setTamanhoPagina(int tamanhoPagina) {
        this.tamanhoPagina = Math.max(10, tamanhoPagina);
        this.paginaAtual = 0;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (dados.length == 0) return 0;
        int inicio = paginaAtual * tamanhoPagina;
        return Math.min(tamanhoPagina, Math.max(0, dados.length - inicio));
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int indiceGlobal = paginaAtual * tamanhoPagina + rowIndex;
        ImagemSatelite img = dados[indiceGlobal];
        return switch (columnIndex) {
            case 0 -> indiceGlobal + 1;
            case 1 -> img.getId();
            case 2 -> img.getSatelite();
            case 3 -> img.getDataCapturaFormatada();
            case 4 -> img.getRegiao();
            case 5 -> String.format(Locale.US, "%.2f", img.getIndiceDesmatamento());
            default -> "";
        };
    }
}
