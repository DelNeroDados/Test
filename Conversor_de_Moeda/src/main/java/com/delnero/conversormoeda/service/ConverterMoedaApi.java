/*
package main.java.com.delnero.conversormoeda.service;

import java.util.Map;

public record ConverterMoedaApi(String base_code, Map<String, Double> conversion_rates) {
    public String result() {
    }
}
*/
package com.delnero.conversormoeda.service;

import java.util.Map;

/*
 * Mapeia a resposta JSON v6 da API ExchangeRate-API com os campos necessários.
 * Campos:
 *   - result: indica "success" ou mensagem de erro
 *   - base_code: código ISO da moeda-base
 *   - conversion_rates: mapa de códigos de moedas e suas taxas de conversão
 */
public record ConverterMoedaApi(
        String result,
        String base_code,
        Map<String, Double> conversion_rates
) {}
