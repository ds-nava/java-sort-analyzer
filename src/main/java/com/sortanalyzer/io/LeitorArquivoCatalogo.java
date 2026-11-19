package com.sortanalyzer.io;

import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.RelatorioLeitura;
import com.sortanalyzer.model.RelatorioLeitura.FormatoDetectado;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Leitor flexível de arquivos externos (.csv ou .txt).
 * Detecta automaticamente se o arquivo segue o formato de catálogo completo
 * ({@code id;satelite;dataCaptura;regiao;indiceDesmatamento}) ou uma lista simples
 * de valores numéricos, reportando linhas corrompidas sem interromper a aplicação.
 */
public final class LeitorArquivoCatalogo {

    private static final int LIMITE_AVISOS = 50;

    private LeitorArquivoCatalogo() {
    }

    public static RelatorioLeitura ler(Path caminho) throws IOException {
        if (caminho == null) {
            throw new IOException("Nenhum caminho de arquivo foi informado.");
        }
        if (!Files.exists(caminho)) {
            throw new IOException("Arquivo não encontrado: " + caminho);
        }
        if (Files.isDirectory(caminho)) {
            throw new IOException("O caminho selecionado é uma pasta, não um arquivo: " + caminho);
        }
        if (!Files.isReadable(caminho)) {
            throw new IOException("Sem permissão de leitura para o arquivo: " + caminho);
        }

        List<String> linhasNaoVazias = new ArrayList<>();
        List<Integer> numerosLinhas = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
            String linha;
            int numLinha = 0;
            while ((linha = br.readLine()) != null) {
                numLinha++;
                if (numLinha == 1 && !linha.isEmpty() && linha.charAt(0) == '\uFEFF') {
                    linha = linha.substring(1); // Remove BOM UTF-8 se existir
                }
                String limpa = linha.trim();
                if (!limpa.isEmpty() && !limpa.startsWith("#")) {
                    linhasNaoVazias.add(limpa);
                    numerosLinhas.add(numLinha);
                }
            }
        }

        if (linhasNaoVazias.isEmpty()) {
            throw new IOException("O arquivo está vazio ou contém apenas linhas em branco/comentários.");
        }

        String primeiraLinha = linhasNaoVazias.get(0);
        boolean ehCatalogo = detectarFormatoCatalogo(primeiraLinha);

        if (ehCatalogo) {
            return lerCatalogoCompleto(linhasNaoVazias, numerosLinhas);
        }
        return lerListaNumerica(linhasNaoVazias, numerosLinhas);
    }

    private static boolean detectarFormatoCatalogo(String primeiraLinha) {
        String[] partes = primeiraLinha.split(";", -1);
        if (partes.length >= 5) {
            // Verifica se parece cabeçalho de catálogo ou linha de registro com texto de satélite/região
            String primeira = partes[0].trim().toLowerCase();
            if (primeira.equals("id") || primeira.contains("imagem") || primeira.contains("codigo")) {
                return true;
            }
            // Se a coluna 1 (satélite) não for puramente numérica, trata-se de catálogo completo
            return !ehNumero(partes[1].trim());
        }
        return false;
    }

    private static RelatorioLeitura lerCatalogoCompleto(List<String> linhas, List<Integer> numLinhas) throws IOException {
        List<ImagemSatelite> registros = new ArrayList<>(linhas.size());
        List<String> avisos = new ArrayList<>();
        int totalAvisos = 0;

        for (int idx = 0; idx < linhas.size(); idx++) {
            String linha = linhas.get(idx);
            int numLinha = numLinhas.get(idx);
            String[] colunas = linha.split(";", -1);

            // Ignora cabeçalho na primeira linha útil
            if (idx == 0 && !ehInteiro(colunas[0].trim())) {
                continue;
            }

            if (colunas.length < 5) {
                totalAvisos++;
                registrarAviso(avisos, "Linha " + numLinha + ": esperadas 5 colunas separadas por ';', encontradas " + colunas.length + ".");
                continue;
            }

            try {
                long id = Long.parseLong(colunas[0].trim());
                String satelite = colunas[1].trim();
                long epoch = parseDataOuEpoch(colunas[2].trim());
                String regiao = colunas[3].trim();
                double indice = Double.parseDouble(colunas[4].trim().replace(',', '.'));
                registros.add(new ImagemSatelite(id, satelite, epoch, regiao, indice));
            } catch (Exception ex) {
                totalAvisos++;
                registrarAviso(avisos, "Linha " + numLinha + " ignorada (" + ex.getMessage() + ").");
            }
        }

        if (totalAvisos > LIMITE_AVISOS) {
            avisos.add("... e mais " + (totalAvisos - LIMITE_AVISOS) + " aviso(s) suprimido(s).");
        }

        if (registros.isEmpty()) {
            throw new IOException("Nenhum registro válido foi encontrado no catálogo CSV. Verifique o formato (id;satelite;data;regiao;indice).");
        }

        return new RelatorioLeitura(
                registros.toArray(new ImagemSatelite[0]),
                FormatoDetectado.CATALOGO_COMPLETO,
                linhas.size(),
                avisos
        );
    }

    private static RelatorioLeitura lerListaNumerica(List<String> linhas, List<Integer> numLinhas) throws IOException {
        List<ImagemSatelite> registros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        int totalAvisos = 0;
        int sequencia = 0;

        for (int idx = 0; idx < linhas.size(); idx++) {
            String linha = linhas.get(idx);
            int numLinha = numLinhas.get(idx);

            // Permite números separados por vírgula, ponto-e-vírgula, espaço ou tabulação
            String[] tokens = linha.split("[\\s,;]+");
            if (idx == 0 && tokens.length == 1 && !ehNumero(tokens[0])) {
                // Provável cabeçalho simples (ex.: "valores" ou "id")
                continue;
            }

            for (String token : tokens) {
                String limpo = token.trim();
                if (limpo.isEmpty()) {
                    continue;
                }
                try {
                    if (!limpo.contains(".")) {
                        long valor = Long.parseLong(limpo);
                        registros.add(new ImagemSatelite(
                                valor,
                                "Arquivo-Externo",
                                GeradorDados.EPOCH_BASE_24H + sequencia,
                                "Importado",
                                (double) valor
                        ));
                    } else {
                        double valor = Double.parseDouble(limpo);
                        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                            throw new NumberFormatException("Valor não finito");
                        }
                        registros.add(new ImagemSatelite(
                                Math.round(valor * 100.0),
                                "Arquivo-Externo",
                                GeradorDados.EPOCH_BASE_24H + sequencia,
                                "Importado",
                                valor
                        ));
                    }
                    sequencia++;
                } catch (NumberFormatException ex) {
                    totalAvisos++;
                    registrarAviso(avisos, "Linha " + numLinha + ": token inválido '" + limpo + "' ignorado.");
                }
            }
        }

        if (totalAvisos > LIMITE_AVISOS) {
            avisos.add("... e mais " + (totalAvisos - LIMITE_AVISOS) + " aviso(s) suprimido(s).");
        }

        if (registros.isEmpty()) {
            throw new IOException("Nenhum valor numérico válido foi encontrado no arquivo selecionado.");
        }

        return new RelatorioLeitura(
                registros.toArray(new ImagemSatelite[0]),
                FormatoDetectado.LISTA_NUMERICA,
                linhas.size(),
                avisos
        );
    }

    private static long parseDataOuEpoch(String texto) {
        if (ehInteiro(texto)) {
            return Long.parseLong(texto);
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(texto, ImagemSatelite.FORMATO_DATA);
            return ldt.toEpochSecond(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("data inválida '" + texto + "' (esperado yyyy-MM-dd HH:mm:ss ou epoch)");
        }
    }

    private static boolean ehInteiro(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean ehNumero(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s.replace(',', '.'));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static void registrarAviso(List<String> avisos, String msg) {
        if (avisos.size() < LIMITE_AVISOS) {
            avisos.add(msg);
        }
    }
}
