package com.sortanalyzer.sort;

/**
 * Exceção sinalizada quando uma execução de ordenação em segundo plano
 * é cancelada pelo usuário.
 */
public final class OrdenacaoCanceladaException extends RuntimeException {

    public OrdenacaoCanceladaException() {
        super("Execução da ordenação cancelada pelo usuário.");
    }
}
