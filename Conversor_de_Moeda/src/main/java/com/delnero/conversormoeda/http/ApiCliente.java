/*
package main.java.com.delnero.conversormoeda.http;

import java.io.IOException;
import java.net.URI;
import java.net.main.java.com.delnero.conversormoeda.http.HttpClient;
import java.net.main.java.com.delnero.conversormoeda.http.HttpRequest;
import java.net.main.java.com.delnero.conversormoeda.http.HttpResponse;
import com.google.gson.Gson;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaLivreApi;  // importe este record

public class ApiCliente {
    private static final String CHAVE_API = "ad2405be8dfb3fca89729582";
    private static final String URL_LIVRE =
            "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%.6f";

    // … seu metodo busca(String) já existente …

    // Busca conversão direta de A → B para um montante
    public ConverterMoedaLivreApi buscaLivre(
            String base, String target, double amount)
            throws IOException, InterruptedException, CustomApiException {

        String url = String.format(URL_LIVRE, CHAVE_API, base, target, amount);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new CustomApiException("Falha HTTP: " + resp.statusCode());
        }

        String json = resp.body();
        System.out.println("JSON livre → " + json);

        ConverterMoedaLivreApi data = new Gson()
                .fromJson(json, ConverterMoedaLivreApi.class);

        if (!"success".equalsIgnoreCase(data.result())) {
            throw new CustomApiException("API retornou: " + data.result());
        }

        return data;
    }
}
*/
package main.java.com.delnero.conversormoeda.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaApi;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

public class ApiCliente {
    private static final String CHAVE_API  = "ad2405be8dfb3fca89729582";
    private static final String URL_FORMAT =
            "https://v6.exchangerate-api.com/v6/%s/latest/%s";

    /**
     * Busca todas as taxas de conversão para a moeda-base (ex: USD)
     */
    public ConverterMoedaApi busca(String base)
            throws IOException, InterruptedException, CustomApiException {
        String url = String.format(URL_FORMAT, CHAVE_API, base);
        HttpRequest  req  = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new CustomApiException("Falha HTTP: " + resp.statusCode());
        }

        ConverterMoedaApi data = new Gson()
                .fromJson(resp.body(), ConverterMoedaApi.class);

        if (!"success".equalsIgnoreCase(data.result())) {
            throw new CustomApiException("API retornou: " + data.result());
        }
        return data;
    }

    /**
     * Converte de base → target multiplicando pela taxa obtida em /latest/{base}
     */
    public ConverterMoedaLivreApi buscaLivre(
            String base, String target, double amount)
            throws IOException, InterruptedException, CustomApiException {

        // 1) puxa todas as taxas
        ConverterMoedaApi apiData = busca(base);

        // 2) recupera apenas a taxa desejada
        Double rate = apiData.conversion_rates().get(target);
        if (rate == null) {
            throw new CustomApiException("Moeda não suportada: " + target);
        }

        // 3) calcula o valor convertido
        double conversionResult = rate * amount;

        // 4) cria o record de resposta
        return new ConverterMoedaLivreApi(
                apiData.result(),
                apiData.base_code(),
                target,
                conversionResult
        );
    }
}
