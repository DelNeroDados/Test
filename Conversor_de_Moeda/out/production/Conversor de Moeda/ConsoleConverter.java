import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import main.java.com.delnero.conversormoeda.http.ApiCliente;
import main.java.com.delnero.conversormoeda.http.CustomApiException;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class ConsoleConverter {
    private static final Path FAVORITES_FILE = Paths.get("favorites.txt");
    private static List<String> favorites = new ArrayList<>();

    public static void main(String[] args) {
        loadFavorites();

        Scanner sc = new Scanner(System.in);
        ApiCliente api = new ApiCliente();

        boolean continuarPrograma = true;
        while (continuarPrograma) {
            // --- Menu principal ---
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

            // --- Loop de conversão para o mesmo par ---
            boolean mesmaPar = true;
            while (mesmaPar) {
                double amount = solicitarMontante(sc);
                try {
                    ConverterMoedaLivreApi result =
                            api.buscaLivre(base, target, amount);
                    System.out.printf(
                            Locale.US,
                            "%.2f %s = %.2f %s%n",
                            amount,
                            result.base_code(),
                            result.conversion_result(),
                            result.target_code()
                    );
                } catch (CustomApiException | IOException |
                         InterruptedException |
                         JsonSyntaxException | JsonIOException e) {
                    System.err.println("Erro ao converter: " + e.getMessage());
                }

                System.out.print("Salvar este par como favorito? (s/n): ");
                String saveFav = sc.nextLine().trim().toLowerCase(Locale.ROOT);
                if (saveFav.startsWith("s")) {
                    String fav = base + "->" + target;
                    if (!favorites.contains(fav)) {
                        favorites.add(fav);
                        appendFavoriteToFile(fav);
                        System.out.println("Favorito salvo!");
                    } else {
                        System.out.println("Este par já está nos favoritos.");
                    }
                }

                System.out.print("Converter outro valor para estas mesmas moedas? (s/n): ");
                String resp = sc.nextLine().trim().toLowerCase(Locale.ROOT);
                mesmaPar = resp.startsWith("s");
            }

            System.out.print("Deseja realizar outra operação com outras moedas? (s/n): ");
            String respGeral = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            if (!respGeral.startsWith("s")) {
                System.out.println("Programa encerrado.");
                break;
            }
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
                System.err.println("Não foi possível carregar favoritos: " + e.getMessage());
            }
        }
    }

    private static void appendFavoriteToFile(String fav) {
        try {
            Files.write(
                    FAVORITES_FILE,
                    Collections.singletonList(fav),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Erro ao salvar favorito: " + e.getMessage());
        }
    }

    private static String selecionarMoedaPorNomeOuCodigo(Scanner sc, String tipo) {
        System.out.printf("Digite a moeda %s (código ou nome): ", tipo);
        String termo = sc.nextLine().trim();
        if (termo.isEmpty()) {
            System.err.println("Entrada vazia, tente novamente.");
            return selecionarMoedaPorNomeOuCodigo(sc, tipo);
        }

        String termoNorm = normalize(termo).toUpperCase(Locale.ROOT);

        List<Currency> matches = Currency.getAvailableCurrencies().stream()
                .filter(c -> {
                    String codeNorm = normalize(c.getCurrencyCode())
                            .toUpperCase(Locale.ROOT);
                    String namePt  = normalize(
                            c.getDisplayName(new Locale("pt", "BR"))
                    ).toUpperCase(Locale.ROOT);
                    String nameEn  = normalize(c.getDisplayName(Locale.ENGLISH))
                            .toUpperCase(Locale.ROOT);
                    return codeNorm.contains(termoNorm)
                            || namePt.contains(termoNorm)
                            || nameEn.contains(termoNorm);
                })
                .sorted(Comparator.comparing(Currency::getCurrencyCode))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            System.err.println("Nenhuma moeda encontrada para: " + termo);
            return selecionarMoedaPorNomeOuCodigo(sc, tipo);
        }
        if (matches.size() == 1) {
            Currency c = matches.get(0);
            System.out.println("Selecionada: " +
                    c.getCurrencyCode() + " - " +
                    c.getDisplayName(new Locale("pt", "BR"))
            );
            return c.getCurrencyCode();
        }

        System.out.println("Múltiplas opções encontradas:");
        int limit = Math.min(matches.size(), 10);
        for (int i = 0; i < limit; i++) {
            Currency c = matches.get(i);
            System.out.printf(
                    "%d) %s - %s%n",
                    i + 1,
                    c.getCurrencyCode(),
                    c.getDisplayName(new Locale("pt", "BR"))
            );
        }
        if (matches.size() > limit) {
            System.out.println(
                    "...e mais " + (matches.size() - limit) + " opções. Refine sua busca."
            );
        }

        System.out.print("Escolha o número da moeda desejada: ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            Currency sel = matches.get(idx);
            System.out.println("Selecionada: " +
                    sel.getCurrencyCode() + " - " +
                    sel.getDisplayName(new Locale("pt", "BR"))
            );
            return sel.getCurrencyCode();
        } catch (Exception e) {
            System.err.println("Opção inválida. Tente novamente.");
            return selecionarMoedaPorNomeOuCodigo(sc, tipo);
        }
    }

    private static double solicitarMontante(Scanner sc) {
        System.out.print("Digite o valor a converter: ");
        String entrada = sc.nextLine().trim().replace(',', '.');
        try {
            return Double.parseDouble(entrada);
        } catch (NumberFormatException e) {
            System.err.println("Valor inválido. Tente novamente.");
            return solicitarMontante(sc);
        }
    }

    private static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
