package com.delnero.conversormoeda.console;

import com.delnero.conversormoeda.http.ApiCliente;
import com.delnero.conversormoeda.http.CustomApiException;
import com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class ConvertendoNoPrompt {
    private static final Path FAVORITES_FILE = Paths.get("favorites.txt");
    private static List<String> favorites = new ArrayList<>();

    public static void main(String[] args) {
        loadFavorites();
        Scanner sc = new Scanner(System.in);
        ApiCliente api = new ApiCliente();

        boolean continuar = true;
        while (continuar) {
            System.out.println("*********************************************************");
            System.out.println(" Seja bem-vindo/a ao Conversor de Moeda =]");
            System.out.println("*********************************************************");
            System.out.println("0) Pesquisa livre de moedas");
            System.out.println("1) Dólar → Peso argentino");
            System.out.println("2) Peso argentino → Dólar");
            System.out.println("3) Dólar → Real brasileiro");
            System.out.println("4) Real brasileiro → Dólar");
            System.out.println("5) Dólar → Peso colombiano");
            System.out.println("6) Peso colombiano → Dólar");
            System.out.println("7) Meus favoritos");
            System.out.println("8) Sair");
            System.out.print("Escolha uma opção válida: ");

            String linha = sc.nextLine().trim();
            int opcao;
            try {
                opcao = Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.err.println("Opção inválida. Tente novamente.");
                continue;
            }

            if (opcao == 8) {
                System.out.println("Programa encerrado.");
                break;
            }

            String base = null, target = null;
            switch (opcao) {
                case 0 -> {
                    base   = selecionarMoedaPorNomeOuCodigo(sc, "origem");
                    target = selecionarMoedaPorNomeOuCodigo(sc, "destino");
                }
                case 1 -> { base = "USD"; target = "ARS"; System.out.println("Dólar → Peso argentino selecionado."); }
                case 2 -> { base = "ARS"; target = "USD"; System.out.println("Peso argentino → Dólar selecionado."); }
                case 3 -> { base = "USD"; target = "BRL"; System.out.println("Dólar → Real brasileiro selecionado."); }
                case 4 -> { base = "BRL"; target = "USD"; System.out.println("Real brasileiro → Dólar selecionado."); }
                case 5 -> { base = "USD"; target = "COP"; System.out.println("Dólar → Peso colombiano selecionado."); }
                case 6 -> { base = "COP"; target = "USD"; System.out.println("Peso colombiano → Dólar selecionado."); }
                case 7 -> {
                    if (favorites.isEmpty()) {
                        System.out.println("Ainda não há favoritos salvos.");
                        continue;
                    }
                    System.out.println("Seus favoritos:");
                    for (int i = 0; i < favorites.size(); i++) {
                        System.out.printf("%d) %s%n", i + 1, favorites.get(i));
                    }
                    System.out.print("Escolha o favorito (número): ");
                    try {
                        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                        String[] parts = favorites.get(idx).split("->");
                        base = parts[0];
                        target = parts[1];
                        System.out.printf("Convertendo favorito: %s → %s%n", base, target);
                    } catch (Exception e) {
                        System.err.println("Seleção inválida.");
                        continue;
                    }
                }
                default -> {
                    System.err.println("Opção inválida. Tente novamente.");
                    continue;
                }
            }

            boolean mesmaPar = true;
            while (mesmaPar) {
                double amount = solicitarMontante(sc);
                try {
                    ConverterMoedaLivreApi res = api.buscaLivre(base, target, amount);
                    System.out.printf(Locale.US, "%.2f %s = %.2f %s%n",
                            amount, res.base_code(), res.conversion_result(), res.target_code());
                } catch (CustomApiException | IOException |
                         InterruptedException |
                         JsonSyntaxException | JsonIOException e) {
                    System.err.println("Erro ao converter: " + e.getMessage());
                }

                System.out.print("Salvar este par como favorito? (s/n): ");
                if (sc.nextLine().trim().equalsIgnoreCase("s")) {
                    String fav = base + "->" + target;
                    if (!favorites.contains(fav)) {
                        favorites.add(fav);
                        appendFavoriteToFile(fav);
                        System.out.println("Favorito salvo!");
                    } else {
                        System.out.println("Já está nos favoritos.");
                    }
                }

                System.out.print("Converter outro valor para estas mesmas moedas? (s/n): ");
                mesmaPar = sc.nextLine().trim().equalsIgnoreCase("s");
            }

            System.out.print("Deseja outra operação? (s/n): ");
            continuar = sc.nextLine().trim().equalsIgnoreCase("s");
        }

        sc.close();
    }

    private static void loadFavorites() {
        if (Files.exists(FAVORITES_FILE)) {
            try {
                favorites = Files.readAllLines(FAVORITES_FILE).stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
            } catch (IOException e) {
                System.err.println("Falha ao carregar favoritos: " + e.getMessage());
            }
        }
    }

    private static void appendFavoriteToFile(String fav) {
        try {
            Files.write(FAVORITES_FILE,
                    Collections.singletonList(fav),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Erro ao salvar favorito: " + e.getMessage());
        }
    }

    private static String selecionarMoedaPorNomeOuCodigo(Scanner sc, String tipo) {
        System.out.printf("Digite a moeda %s (código ou nome): ", tipo);
        String termo = sc.nextLine().trim();
        if (termo.isEmpty()) {
            System.err.println("Entrada vazia. Tente novamente.");
            return selecionarMoedaPorNomeOuCodigo(sc, tipo);
        }
        String norm = normalize(termo).toUpperCase(Locale.ROOT);
        List<Currency> matches = Currency.getAvailableCurrencies().stream()
                .filter(c -> normalize(c.getCurrencyCode()).toUpperCase().contains(norm)
                        || normalize(c.getDisplayName(new Locale("pt","BR"))).toUpperCase().contains(norm)
                        || normalize(c.getDisplayName(Locale.ENGLISH)).toUpperCase().contains(norm))
                .sorted(Comparator.comparing(Currency::getCurrencyCode))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            System.err.println("Nenhuma moeda para: " + termo);
            return selecionarMoedaPorNomeOuCodigo(sc, tipo);
        } else if (matches.size() == 1) {
            Currency c = matches.get(0);
            System.out.printf("Selecionada: %s - %s%n",
                    c.getCurrencyCode(), c.getDisplayName(new Locale("pt","BR")));
            return c.getCurrencyCode();
        } else {
            System.out.println("Múltiplas opções:");
            for (int i = 0; i < Math.min(matches.size(), 10); i++) {
                Currency c = matches.get(i);
                System.out.printf("%d) %s - %s%n",
                        i+1,
                        c.getCurrencyCode(),
                        c.getDisplayName(new Locale("pt","BR")));
            }
            System.out.print("Escolha o número: ");
            try {
                int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                Currency sel = matches.get(idx);
                System.out.printf("Selecionada: %s - %s%n",
                        sel.getCurrencyCode(), sel.getDisplayName(new Locale("pt","BR")));
                return sel.getCurrencyCode();
            } catch (Exception e) {
                System.err.println("Inválido. Tente novamente.");
                return selecionarMoedaPorNomeOuCodigo(sc, tipo);
            }
        }
    }

    private static double solicitarMontante(Scanner sc) {
        System.out.print("Valor a converter: ");
        String in = sc.nextLine().trim().replace(',', '.');
        try {
            return Double.parseDouble(in);
        } catch (NumberFormatException e) {
            System.err.println("Valor inválido. Tente de novo.");
            return solicitarMontante(sc);
        }
    }

    private static String normalize(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
