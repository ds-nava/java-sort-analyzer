package com.sortanalyzer.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa o registro catalogado de uma imagem capturada por satélite
 * durante o monitoramento ambiental da Floresta Amazônica.
 */
public final class ImagemSatelite {

    public static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final long id;
    private final String satelite;
    private final long dataCapturaEpochSeg;
    private final String regiao;
    private final double indiceDesmatamento;

    public ImagemSatelite(long id,
                          String satelite,
                          long dataCapturaEpochSeg,
                          String regiao,
                          double indiceDesmatamento) {
        if (satelite == null || satelite.isBlank()) {
            throw new IllegalArgumentException("O nome do satélite não pode ser vazio.");
        }
        if (regiao == null || regiao.isBlank()) {
            throw new IllegalArgumentException("A região de monitoramento não pode ser vazia.");
        }
        if (Double.isNaN(indiceDesmatamento) || Double.isInfinite(indiceDesmatamento)) {
            throw new IllegalArgumentException("O índice de desmatamento deve ser um número válido.");
        }
        this.id = id;
        this.satelite = satelite.trim();
        this.dataCapturaEpochSeg = dataCapturaEpochSeg;
        this.regiao = regiao.trim();
        this.indiceDesmatamento = indiceDesmatamento;
    }

    public long getId() {
        return id;
    }

    public String getSatelite() {
        return satelite;
    }

    public long getDataCapturaEpochSeg() {
        return dataCapturaEpochSeg;
    }

    public String getDataCapturaFormatada() {
        LocalDateTime ldt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dataCapturaEpochSeg), ZoneOffset.UTC);
        return FORMATO_DATA.format(ldt);
    }

    public String getRegiao() {
        return regiao;
    }

    public double getIndiceDesmatamento() {
        return indiceDesmatamento;
    }

    public String paraLinhaCsv() {
        return String.format(Locale.US, "%d;%s;%s;%s;%.2f",
                id, satelite, getDataCapturaFormatada(), regiao, indiceDesmatamento);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImagemSatelite outro)) return false;
        return id == outro.id
                && dataCapturaEpochSeg == outro.dataCapturaEpochSeg
                && Double.compare(outro.indiceDesmatamento, indiceDesmatamento) == 0
                && satelite.equals(outro.satelite)
                && regiao.equals(outro.regiao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, satelite, dataCapturaEpochSeg, regiao, indiceDesmatamento);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "ImagemSatelite{id=%d, satelite='%s', data='%s', regiao='%s', indice=%.2f%%}",
                id, satelite, getDataCapturaFormatada(), regiao, indiceDesmatamento);
    }
}
