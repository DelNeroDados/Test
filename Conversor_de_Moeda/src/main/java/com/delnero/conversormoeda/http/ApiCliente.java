package com.delnero.conversormoeda.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.delnero.conversormoeda.service.ConverterMoedaApi;
import com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

/**
 * Cliente HTTP para a ExchangeRate-API v6.
 */
public class ApiCliente {
    // Você pode definir EXCHANGE_API_KEY como variável de ambiente
    // ou deixar este hard-code temporário para testes rápidos.
    private static final String CHAVE_API =
            System.getenv("EXCHANGE_API_KEY") != null
                    && !System.getenv("EXCHANGE_API_KEY").isBlank()
                    ? System.getenv("EXCHANGE_API_KEY")
                    : "ad2405be8dfb3fca89729582";

    private static final String URL_FORMAT =
            "https://v6.exchangerate-api.com/v6/%s/latest/%s";

    /**
     * Busca todas as taxas de conversão para a moeda-base.
     *
     * @param base código ISO da moeda base (ex: "USD")
     * @return objeto com result, base_code e conversion_rates
     * @throws IOException
     * @throws InterruptedException
     * @throws CustomApiException se HTTP != 200 ou result != "success"
     */
    public ConverterMoedaApi busca(String base)
            throws IOException, InterruptedException, CustomApiException {

        String url = String.format(URL_FORMAT, CHAVE_API, base);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new CustomApiException("Falha HTTP: " + response.statusCode());
        }

        ConverterMoedaApi data = new Gson()
                .fromJson(response.body(), ConverterMoedaApi.class);

        if (!"success".equalsIgnoreCase(data.result())) {
            throw new CustomApiException("API retornou: " + data.result());
        }

        return data;
    }

    /**
     * Converte de base → target multiplicando amount pela taxa obtida
     * em /latest/{base}.
     *
     * @param base   código ISO da moeda base
     * @param target código ISO da moeda alvo
     * @param amount valor a converter
     * @return record com result, base_code, target_code e conversion_result
     * @throws IOException
     * @throws InterruptedException
     * @throws CustomApiException se a moeda não for suportada ou houver erro na API
     */
    public ConverterMoedaLivreApi buscaLivre(
            String base, String target, double amount)
            throws IOException, InterruptedException, CustomApiException {

        // 1) obtém todas as taxas de "base"
        ConverterMoedaApi apiData = busca(base);

        // 2) pega a taxa para target
        Double rate = apiData.conversion_rates().get(target);
        if (rate == null) {
            throw new CustomApiException("Moeda não suportada: " + target);
        }

        // 3) calcula o valor convertido
        double conversionResult = rate * amount;

        // 4) retorna o resultado encapsulado
        return new ConverterMoedaLivreApi(
                apiData.result(),
                apiData.base_code(),
                target,
                conversionResult
        );
    }
}
