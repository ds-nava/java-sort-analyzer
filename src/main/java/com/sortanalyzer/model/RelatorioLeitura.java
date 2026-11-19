package com.sortanalyzer.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsula o resultado da leitura de um arquivo externo, incluindo o formato
 * detectado automaticamente e os eventuais avisos de linhas ignoradas/inválidas.
 */
public final class RelatorioLeitura {

    public enum FormatoDetectado {
        CATALOGO_COMPLETO("Catálogo Completo (CSV com 5 colunas)"),
        LISTA_NUMERICA("Lista Numérica Simples (convertida para registros de satélite)");

        private final String descricao;

        FormatoDetectado(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private final ImagemSatelite[] registros;
    private final FormatoDetectado formatoDetectado;
    private final int linhasProcessadas;
    private final List<String> avisos;

    public RelatorioLeitura(ImagemSatelite[] registros,
                            FormatoDetectado formatoDetectado,
                            int linhasProcessadas,
                            List<String> avisos) {
        this.registros = registros;
        this.formatoDetectado = formatoDetectado;
        this.linhasProcessadas = linhasProcessadas;
        this.avisos = Collections.unmodifiableList(avisos);
    }

    public ImagemSatelite[] getRegistros() {
        return registros;
    }

    public FormatoDetectado getFormatoDetectado() {
        return formatoDetectado;
    }

    public int getLinhasProcessadas() {
        return linhasProcessadas;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public boolean possuiAvisos() {
        return !avisos.isEmpty();
    }
}
