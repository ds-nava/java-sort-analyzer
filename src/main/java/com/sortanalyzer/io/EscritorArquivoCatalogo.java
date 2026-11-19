package com.sortanalyzer.io;

import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.ResultadoOrdenacao;
import com.sortanalyzer.model.TipoCenario;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exporta conjuntos de {@link ImagemSatelite}, resultados de benchmark em CSV
 * e gera arquivos de exemplo prontos para testes externos.
 */
public final class EscritorArquivoCatalogo {

    private EscritorArquivoCatalogo() {
    }

    /**
     * Salva um vetor de imagens no formato catálogo completo CSV delimitado por ';'.
     */
    public static void exportarCatalogoCsv(Path destino, ImagemSatelite[] dados) throws IOException {
        garantirDiretorioPai(destino);
        try (BufferedWriter bw = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            bw.write('\uFEFF'); // BOM UTF-8 para compatibilidade direta com Microsoft Excel
            bw.write("id;satelite;data_captura;regiao;indice_desmatamento");
            bw.newLine();
            for (ImagemSatelite img : dados) {
                bw.write(img.paraLinhaCsv());
                bw.newLine();
            }
        }
    }

    /**
     * Salva apenas os IDs numéricos (um por linha) em arquivo texto simples.
     */
    public static void exportarListaSimplesTxt(Path destino, ImagemSatelite[] dados) throws IOException {
        garantirDiretorioPai(destino);
        try (BufferedWriter bw = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            for (ImagemSatelite img : dados) {
                bw.write(Long.toString(img.getId()));
                bw.newLine();
            }
        }
    }

    /**
     * Exporta resultados comparativos de performance em CSV para análise em planilhas e gráficos.
     */
    public static void exportarResultadosCsv(Path destino, List<ResultadoOrdenacao> resultados) throws IOException {
        garantirDiretorioPai(destino);
        try (BufferedWriter bw = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            bw.write('\uFEFF');
            bw.write("algoritmo;complexidade_media;complexidade_pior;estavel;tamanho;cenario;criterio;tempo_ms;repeticoes;verificacao");
            bw.newLine();
            for (ResultadoOrdenacao r : resultados) {
                bw.write(r.paraLinhaCsv());
                bw.newLine();
            }
        }
    }

    /**
     * Gera um conjunto completo de arquivos externos de teste (1.000, 10.000, 100.000 imagens,
     * lista numérica simples e arquivo contendo erros propositais para validação).
     */
    public static void gerarArquivosExemplo(Path diretorio, boolean incluir100k) throws IOException {
        Files.createDirectories(diretorio);

        exportarCatalogoCsv(
                diretorio.resolve("catalogo_1000.csv"),
                GeradorDados.gerar(1_000, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, GeradorDados.SEED_PADRAO)
        );
        exportarCatalogoCsv(
                diretorio.resolve("catalogo_10000.csv"),
                GeradorDados.gerar(10_000, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, GeradorDados.SEED_PADRAO)
        );
        if (incluir100k) {
            exportarCatalogoCsv(
                    diretorio.resolve("catalogo_100000.csv"),
                    GeradorDados.gerar(100_000, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, GeradorDados.SEED_PADRAO)
            );
        }

        exportarListaSimplesTxt(
                diretorio.resolve("numeros_simples.txt"),
                GeradorDados.gerar(200, TipoCenario.ALEATORIO, CriterioOrdenacao.ID, 123L)
        );

        // Arquivo com linhas válidas e inválidas para demonstrar o tratamento de erros
        Path arquivoErros = diretorio.resolve("arquivo_com_erros.csv");
        try (BufferedWriter bw = Files.newBufferedWriter(arquivoErros, StandardCharsets.UTF_8)) {
            bw.write("id;satelite;data_captura;regiao;indice_desmatamento\n");
            bw.write("50120;Amazônia-1;2026-06-15 08:12:00;AM-Oeste;14.50\n");
            bw.write("ID_INVALIDO;CBERS-4A;2026-06-15 08:15:00;PA-Sudoeste;22.10\n");
            bw.write("10402;Sentinel-2A;2026-06-15 09:05:30;MT-Norte;8.75\n");
            bw.write("30999;Landsat-9;DATA_CORROMPIDA;RO-Central;31.20\n");
            bw.write("20810;CBERS-4A;2026-06-15 10:40:15;AC-Leste;19.00\n");
            bw.write("COLUNA_FALTANDO;Amazônia-1\n");
            bw.write("15200;Aqua-MODIS;2026-06-15 11:22:05;RR-Sul;5.40\n");
        }
    }

    private static void garantirDiretorioPai(Path destino) throws IOException {
        Path pai = destino.toAbsolutePath().getParent();
        if (pai != null) {
            Files.createDirectories(pai);
        }
    }
}
