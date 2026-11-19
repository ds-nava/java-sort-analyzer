package com.sortanalyzer.model;

import java.util.Comparator;

/**
 * Define a chave pela qual os registros de {@link ImagemSatelite} serão ordenados.
 */
public enum CriterioOrdenacao {

    ID("ID da Imagem", Comparator.comparingLong(ImagemSatelite::getId)),
    DATA_CAPTURA("Data/Hora de Captura", Comparator.comparingLong(ImagemSatelite::getDataCapturaEpochSeg)),
    INDICE_DESMATAMENTO("Índice de Desmatamento (%)", Comparator.comparingDouble(ImagemSatelite::getIndiceDesmatamento));

    private final String rotulo;
    private final Comparator<ImagemSatelite> comparador;

    CriterioOrdenacao(String rotulo, Comparator<ImagemSatelite> comparador) {
        this.rotulo = rotulo;
        this.comparador = comparador;
    }

    public String getRotulo() {
        return rotulo;
    }

    public Comparator<ImagemSatelite> getComparador() {
        return comparador;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
