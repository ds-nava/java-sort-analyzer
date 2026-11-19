package com.sortanalyzer.io;

import com.sortanalyzer.model.CriterioOrdenacao;
import com.sortanalyzer.model.ImagemSatelite;
import com.sortanalyzer.model.TipoCenario;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Gera bases determinísticas de registros {@link ImagemSatelite} para os cenários
 * de teste e realiza o parsing de entradas manuais digitadas pelo usuário.
 */
public final class GeradorDados {

    public static final long SEED_PADRAO = 42L;
    public static final long EPOCH_BASE_24H =
            LocalDateTime.of(2026, 6, 15, 0, 0, 0).toEpochSecond(ZoneOffset.UTC);

    private static final String[] SATELITES = {
            "Amazônia-1", "CBERS-4A", "Sentinel-2A", "Landsat-9", "Aqua-MODIS"
    };

    private static final String[] REGIOES = {
            "AM-Oeste", "AM-Sul", "PA-Sudoeste", "PA-Nordeste",
            "MT-Norte", "RO-Central", "AC-Leste", "RR-Sul", "AP-Norte"
    };

    private GeradorDados() {
    }

    /**
     * Gera um conjunto de {@code quantidade} imagens de satélite distribuído conforme o cenário
     * e o critério de ordenação selecionados, utilizando uma semente fixa para reprodutibilidade.
     */
    public static ImagemSatelite[] gerar(int quantidade,
                                         TipoCenario cenario,
                                         CriterioOrdenacao criterio,
                                         long seed) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade de registros deve ser maior ou igual a 1.");
        }
        if (quantidade > 500_000) {
            throw new IllegalArgumentException("Limite máximo suportado pelo gerador: 500.000 registros.");
        }
        if (cenario == null || criterio == null) {
            throw new IllegalArgumentException("Cenário e critério de ordenação são obrigatórios.");
        }

        Random rng = new Random(seed);
        ImagemSatelite[] dados = new ImagemSatelite[quantidade];

        // Gera a mesma base determinística para um dado (quantidade, seed)
        int maxId = Math.max(quantidade * 10, 1_000_000);
        for (int i = 0; i < quantidade; i++) {
            long id = 100_000L + rng.nextInt(maxId);
            String satelite = SATELITES[rng.nextInt(SATELITES.length)];
            long epoch = EPOCH_BASE_24H + rng.nextInt(86_400); // janela de 24 horas
            String regiao = REGIOES[rng.nextInt(REGIOES.length)];
            double indice = Math.round(rng.nextDouble() * 10000.0) / 100.0; // 0.00 a 100.00%
            dados[i] = new ImagemSatelite(id, satelite, epoch, regiao, indice);
        }

        aplicarDisposicaoInicial(dados, cenario, criterio, new Random(seed ^ 0x9E3779B97F4A7C15L));
        return dados;
    }

    private static void aplicarDisposicaoInicial(ImagemSatelite[] dados,
                                                 TipoCenario cenario,
                                                 CriterioOrdenacao criterio,
                                                 Random rng) {
        switch (cenario) {
            case ORDENADO -> Arrays.sort(dados, criterio.getComparador());
            case INVERSO -> Arrays.sort(dados, criterio.getComparador().reversed());
            case SEMI_ORDENADO -> {
                Arrays.sort(dados, criterio.getComparador());
                // Perturba aproximadamente 10% dos elementos por meio de 5% de trocas aleatórias
                int trocas = Math.max(1, dados.length / 20);
                for (int k = 0; k < trocas; k++) {
                    int a = rng.nextInt(dados.length);
                    int b = rng.nextInt(dados.length);
                    ImagemSatelite tmp = dados[a];
                    dados[a] = dados[b];
                    dados[b] = tmp;
                }
            }
            case ALEATORIO, EXTERNO, MANUAL -> {
                // Embaralhamento Fisher-Yates garantindo aleatoriedade uniforme
                for (int i = dados.length - 1; i > 0; i--) {
                    int j = rng.nextInt(i + 1);
                    ImagemSatelite tmp = dados[i];
                    dados[i] = dados[j];
                    dados[j] = tmp;
                }
            }
        }
    }

    /**
     * Converte uma sequência de valores numéricos digitados manualmente pelo usuário
     * (separados por vírgula, ponto-e-vírgula, espaço ou quebra de linha) em registros {@link ImagemSatelite}.
     */
    public static ImagemSatelite[] converterEntradaManual(String textoBruto) {
        if (textoBruto == null || textoBruto.isBlank()) {
            throw new IllegalArgumentException("Digite pelo menos 2 valores numéricos para ordenar.");
        }

        String[] tokens = textoBruto.trim().split("[\\s,;]+");
        List<String> validos = new ArrayList<>();
        for (String t : tokens) {
            if (!t.isBlank()) {
                validos.add(t.trim());
            }
        }
        if (validos.size() < 2) {
            throw new IllegalArgumentException("Informe pelo menos 2 números separados por vírgula, espaço ou linha.");
        }

        boolean contemDecimal = validos.stream().anyMatch(s -> s.contains("."));
        ImagemSatelite[] registros = new ImagemSatelite[validos.size()];

        for (int i = 0; i < validos.size(); i++) {
            String token = validos.get(i);
            try {
                if (!contemDecimal) {
                    long valor = Long.parseLong(token);
                    registros[i] = new ImagemSatelite(
                            valor,
                            "Entrada-Manual",
                            EPOCH_BASE_24H + i,
                            "Manual",
                            (double) valor
                    );
                } else {
                    double valor = Double.parseDouble(token);
                    if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                        throw new NumberFormatException("Valor não finito");
                    }
                    registros[i] = new ImagemSatelite(
                            Math.round(valor * 100.0),
                            "Entrada-Manual",
                            EPOCH_BASE_24H + i,
                            "Manual",
                            valor
                    );
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Valor inválido encontrado na posição " + (i + 1) + ": '" + token + "'. Use apenas números (ex.: 45, 12, 99, 7).");
            }
        }

        return registros;
    }

    /**
     * Gera sequência visual de valores inteiros (10..100) usada no Visualizador Animado.
     */
    public static int[] gerarSequenciaVisual(int quantidade, TipoCenario cenario, long seed) {
        if (quantidade < 5 || quantidade > 200) {
            throw new IllegalArgumentException("A quantidade visual deve estar entre 5 e 200 barras.");
        }
        int[] valores = new int[quantidade];
        for (int i = 0; i < quantidade; i++) {
            // Alturas proporcionais bem distribuídas entre 8 e 100
            valores[i] = 8 + (int) Math.round((92.0 * i) / Math.max(1, quantidade - 1));
        }

        Random rng = new Random(seed);
        switch (cenario) {
            case ORDENADO -> {
                // Já está crescente
            }
            case INVERSO -> {
                for (int i = 0; i < quantidade / 2; i++) {
                    int tmp = valores[i];
                    valores[i] = valores[quantidade - 1 - i];
                    valores[quantidade - 1 - i] = tmp;
                }
            }
            case SEMI_ORDENADO -> {
                int trocas = Math.max(1, quantidade / 12);
                for (int k = 0; k < trocas; k++) {
                    int a = rng.nextInt(quantidade);
                    int b = rng.nextInt(quantidade);
                    int tmp = valores[a];
                    valores[a] = valores[b];
                    valores[b] = tmp;
                }
            }
            default -> {
                for (int i = quantidade - 1; i > 0; i--) {
                    int j = rng.nextInt(i + 1);
                    int tmp = valores[i];
                    valores[i] = valores[j];
                    valores[j] = tmp;
                }
            }
        }
        return valores;
    }
}
