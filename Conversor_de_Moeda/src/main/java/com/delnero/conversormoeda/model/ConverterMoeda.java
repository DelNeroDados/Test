package com.delnero.conversormoeda.model;

import com.delnero.conversormoeda.service.ConverterMoedaApi;
import com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

import java.util.Map;

public class ConverterMoeda {
    private String codigoBase;
    private String codigoConverter;
    private Map<String, Double> taxaConversao;
    private Double taxaConversaoLivre;
    private Double taxaResultante;

    public ConverterMoeda(ConverterMoedaApi moedaApi) {
        this.codigoBase = moedaApi.base_code();
        this.taxaConversao = moedaApi.conversion_rates();
    }

    public ConverterMoeda(ConverterMoedaLivreApi moedaLivreApi) {
        this.codigoBase = moedaLivreApi.base_code();
        this.codigoConverter = moedaLivreApi.target_code();
        this.taxaConversaoLivre = moedaLivreApi.conversion_result();
        this.taxaResultante = moedaLivreApi.conversion_result();
    }

    public String getCodigoBase() {
        return codigoBase;
    }

    public void setCodigoBase(String codigoBase) {
        this.codigoBase = codigoBase;
    }

    public Map<String, Double> getTaxaConversao() {
        return taxaConversao;
    }

    public void setTaxaConversao(Map<String, Double> taxaConversao) {
        this.taxaConversao = taxaConversao;
    }

    public String getCodigoConverter() {
        return codigoConverter;
    }

    public void setCodigoConverter(String codigoConverter) {
        this.codigoConverter = codigoConverter;
    }

    public Double getTaxaResultante() {
        return taxaResultante;
    }

    public void setTaxaResultante( Double taxaResultante) {
        this.taxaResultante = taxaResultante;
    }

    public Double getTaxaConversaoLivre() {
        return taxaConversaoLivre;
    }

    public void setTaxaConversaoLivre(Double taxaConversaoLivre) {
        this.taxaConversaoLivre = taxaConversaoLivre;
    }

    @Override
    public String toString() {
        return "ConverterMoeda{" +
                "codigoBase='" + codigoBase + '\'' +
                ", taxaConversao=" + taxaConversao +
                '}';
    }
}
