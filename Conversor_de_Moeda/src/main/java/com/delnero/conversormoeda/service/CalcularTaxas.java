package com.delnero.conversormoeda.service;

import com.delnero.conversormoeda.model.ConverterMoeda;

import java.util.Map;

public class CalcularTaxas extends ConverterMoeda {
    private final Map<String, Double> taxasConversao;

    public CalcularTaxas(ConverterMoedaApi moedaApi) {
        super(moedaApi);
        this.taxasConversao = moedaApi.conversion_rates();
    }

    public double calcularConversao(String moeda, double valor) {
        Double taxa = taxasConversao.get(moeda);
        if (taxa != null) {
            return valor * taxa;
        } else {
            throw new IllegalArgumentException("Moeda não encontrada: " + moeda);
        }
    }
}
