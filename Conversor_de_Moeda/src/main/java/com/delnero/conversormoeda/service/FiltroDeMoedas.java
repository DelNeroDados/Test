package com.delnero.conversormoeda.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FiltroDeMoedas {
    private static final List<String> listaDeMoedas = Arrays.asList("ARS", "BOB", "BRL", "CLP", "COP", "USD");

    public Map<String, Double> filtrarMoedas(Map<String, Double> listaDeMoedasApi){
        return listaDeMoedasApi
                .entrySet()
                .stream()
                .filter(entrada -> listaDeMoedas.contains(entrada.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String selecionarMoeda(Map<String, Double> moedasFiltradas, String moedaSelecionada) {
        if (moedasFiltradas.containsKey(moedaSelecionada)) {
            return moedaSelecionada;
        } else {
            throw new IllegalArgumentException("Moeda não permitida ou não encontrada: " + moedaSelecionada);
        }
    }
}
