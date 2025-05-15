// se você usar package, descomente esta linha e ajuste:
// package main;
/*
import main.java.com.delnero.conversormoeda.http.ApiCliente;
import main.java.com.delnero.conversormoeda.http.CustomApiException;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CurrencyConverterUI extends JFrame {
    private final JComboBox<String> cbBase;
    private final JComboBox<String> cbTarget;
    private final JTextField tfAmount;
    private final JLabel lblResult;
    private final DefaultListModel<String> favModel;
    private final JList<String> listFavs;
    private final ApiCliente api;

    public CurrencyConverterUI() {
        super("Conversor de Moedas");
        this.api = new ApiCliente();

        List<String> codes = Currency.getAvailableCurrencies().stream()
                .map(Currency::getCurrencyCode)
                .sorted()
                .collect(Collectors.toList());

        cbBase   = new JComboBox<>(codes.toArray(new String[0]));
        cbTarget = new JComboBox<>(codes.toArray(new String[0]));
        tfAmount = new JTextField("1.00", 10);
        lblResult= new JLabel("Resultado: ---");

        favModel = new DefaultListModel<>();
        listFavs = new JList<>(favModel);
        loadFavorites();

        JButton btnConvert = new JButton("Converter");
        JButton btnSaveFav = new JButton("Salvar Favorito");
        JButton btnLoadFav= new JButton("Usar Favorito");

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);

        c.gridx=0; c.gridy=0; add(new JLabel("Base:"), c);
        c.gridx=1; add(cbBase, c);
        c.gridx=0; c.gridy=1; add(new JLabel("Target:"), c);
        c.gridx=1; add(cbTarget, c);
        c.gridx=0; c.gridy=2; add(new JLabel("Valor:"), c);
        c.gridx=1; add(tfAmount, c);

        c.gridx=0; c.gridy=3; add(btnConvert, c);
        c.gridx=1; add(lblResult, c);

        c.gridx=0; c.gridy=4; add(btnSaveFav, c);
        c.gridx=1; add(btnLoadFav, c);

        c.gridx=0; c.gridy=5; c.gridwidth=2;
        add(new JScrollPane(listFavs), c);

        btnConvert.addActionListener(e -> doConvert());
        btnSaveFav.addActionListener(e -> saveFavorite());
        btnLoadFav.addActionListener(e -> loadSelectedFavorite());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void doConvert() {
        String base   = (String) cbBase.getSelectedItem();
        String target = (String) cbTarget.getSelectedItem();
        double amount;
        try {
            amount = Double.parseDouble(tfAmount.getText().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            ConverterMoedaLivreApi res = api.buscaLivre(base, target, amount);
            lblResult.setText(String.format(
                    Locale.US,
                    "Resultado: %.2f %s = %.2f %s",
                    amount, res.base_code(), res.conversion_result(), res.target_code()
            ));
        } catch (CustomApiException|IOException|InterruptedException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Conversão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFavorite() {
        String fav = cbBase.getSelectedItem() + "->" + cbTarget.getSelectedItem();
        if (!favModel.contains(fav)) {
            favModel.addElement(fav);
            appendFavoriteToFile(fav);
        }
    }

    private void loadSelectedFavorite() {
        String fav = listFavs.getSelectedValue();
        if (fav != null && fav.contains("->")) {
            String[] p = fav.split("->");
            cbBase.setSelectedItem(p[0]);
            cbTarget.setSelectedItem(p[1]);
        }
    }

    private void loadFavorites() {
        Path file = Paths.get("favorites.txt");
        if (Files.exists(file)) {
            try {
                Files.readAllLines(file).stream()
                        .map(String::trim)
                        .filter(s -> s.contains("->"))
                        .forEach(favModel::addElement);
            } catch (IOException e) {
                // ignora
            }
        }
    }
    private void appendFavoriteToFile(String fav) {
        try {
            Files.writeString(
                    Paths.get("favorites.txt"),
                    fav + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            // ignora
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(CurrencyConverterUI::new);
    }
}
*/


/*
// src/CurrencyConverterUI.java

import main.java.com.delnero.conversormoeda.http.ApiCliente;
import main.java.com.delnero.conversormoeda.http.CustomApiException;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CurrencyConverterUI extends JFrame {
    private final ApiCliente api = new ApiCliente();

    private final JComboBox<String> menuCombo;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    // Components for “Pesquisa livre”
    private final JTextField tfBaseFree  = new JTextField(15);
    private final JTextField tfTargetFree= new JTextField(15);
    private final JTextField tfAmountFree= new JTextField("1.00", 10);
    private final JLabel lblResultFree   = new JLabel("Resultado: ---");
    private final JButton btnConvertFree = new JButton("Converter");

    // Components for presets (1–6)
    private final JTextField tfAmountPreset = new JTextField("1.00", 10);
    private final JLabel lblPresetPair      = new JLabel();
    private final JLabel lblResultPreset    = new JLabel("Resultado: ---");
    private final JButton btnConvertPreset  = new JButton("Converter");

    public CurrencyConverterUI() {
        super("Conversor de Moedas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // 1) Top: menu combo + Go button
        String[] menus = {
                "0) Pesquisa livre",
                "1) USD → ARS",
                "2) ARS → USD",
                "3) USD → BRL",
                "4) BRL → USD",
                "5) USD → COP",
                "6) COP → USD",
                "7) Sair"
        };
        menuCombo = new JComboBox<>(menus);
        JButton btnGo = new JButton("OK");
        JPanel top = new JPanel();
        top.add(new JLabel("Selecione:"));
        top.add(menuCombo);
        top.add(btnGo);
        add(top, BorderLayout.NORTH);

        // 2) Center: card panel with two cards
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 2a) Card “free”
        JPanel freePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4); c.anchor = GridBagConstraints.WEST;

        c.gridx=0; c.gridy=0; freePanel.add(new JLabel("Moeda origem (código ou nome):"), c);
        c.gridx=1; freePanel.add(tfBaseFree, c);
        c.gridx=0; c.gridy=1; freePanel.add(new JLabel("Moeda destino (código ou nome):"), c);
        c.gridx=1; freePanel.add(tfTargetFree, c);
        c.gridx=0; c.gridy=2; freePanel.add(new JLabel("Valor:"), c);
        c.gridx=1; freePanel.add(tfAmountFree, c);
        c.gridx=0; c.gridy=3; freePanel.add(btnConvertFree, c);
        c.gridx=1; freePanel.add(lblResultFree, c);

        // converter ação
        btnConvertFree.addActionListener(e -> {
            String baseInput = tfBaseFree.getText().trim();
            String targetInput = tfTargetFree.getText().trim();
            String base = resolveCurrency(baseInput);
            String target = resolveCurrency(targetInput);
            if (base == null || target == null) return;
            double amount;
            try {
                amount = Double.parseDouble(tfAmountFree.getText().replace(',', '.'));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            doConvert(base, target, amount, lblResultFree);
        });

        // 2b) Card “preset”
        JPanel presetPanel = new JPanel(new GridBagLayout());
        c.gridx=0; c.gridy=0; presetPanel.add(new JLabel("Par de moedas:"), c);
        c.gridx=1; presetPanel.add(lblPresetPair, c);
        c.gridx=0; c.gridy=1; presetPanel.add(new JLabel("Valor:"), c);
        c.gridx=1; presetPanel.add(tfAmountPreset, c);
        c.gridx=0; c.gridy=2; presetPanel.add(btnConvertPreset, c);
        c.gridx=1; presetPanel.add(lblResultPreset, c);

        btnConvertPreset.addActionListener(e -> {
            // base/target já definidos ao trocar o menu
            String text = lblPresetPair.getText(); // ex: "USD → ARS"
            String[] parts = text.split("→");
            if (parts.length != 2) return;
            String base = parts[0].trim();
            String target = parts[1].trim();
            double amount;
            try {
                amount = Double.parseDouble(tfAmountPreset.getText().replace(',', '.'));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            doConvert(base, target, amount, lblResultPreset);
        });

        cardPanel.add(freePanel, "FREE");
        cardPanel.add(presetPanel, "PRESET");
        add(cardPanel, BorderLayout.CENTER);

        // Ao clicar “OK”
        btnGo.addActionListener(e -> updateCard());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateCard() {
        int idx = menuCombo.getSelectedIndex();
        if (idx == 7) {
            System.exit(0);
        } else if (idx == 0) {
            cardLayout.show(cardPanel, "FREE");
        } else {
            // presets 1–6
            String[][] presets = {
                    {}, // placeholder
                    {"USD","ARS"},
                    {"ARS","USD"},
                    {"USD","BRL"},
                    {"BRL","USD"},
                    {"USD","COP"},
                    {"COP","USD"}
            };
            String[] pair = presets[idx];
            lblPresetPair.setText(pair[0] + " → " + pair[1]);
            lblResultPreset.setText("Resultado: ---");
            tfAmountPreset.setText("1.00");
            cardLayout.show(cardPanel, "PRESET");
        }
        pack();
    }

    private void doConvert(String base, String target, double amount, JLabel output) {
        try {
            ConverterMoedaLivreApi res = api.buscaLivre(base, target, amount);
            output.setText(String.format(
                    Locale.US,
                    "%.2f %s = %.2f %s",
                    amount, res.base_code(), res.conversion_result(), res.target_code()
            ));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Resolve um termo (código ou nome pt/en) para o código ISO, ou mostra dialog de escolha.

    private String resolveCurrency(String termo) {
        if (termo.isBlank()) {
            JOptionPane.showMessageDialog(this, "Entrada vazia.", "Erro", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String norm = normalize(termo).toUpperCase(Locale.ROOT);
        List<Currency> list = Currency.getAvailableCurrencies().stream()
                .filter(c -> {
                    String code = normalize(c.getCurrencyCode()).toUpperCase(Locale.ROOT);
                    String namePt = normalize(c.getDisplayName(new Locale("pt","BR")))
                            .toUpperCase(Locale.ROOT);
                    String nameEn = normalize(c.getDisplayName(Locale.ENGLISH))
                            .toUpperCase(Locale.ROOT);
                    return code.contains(norm) || namePt.contains(norm) || nameEn.contains(norm);
                })
                .sorted(Comparator.comparing(Currency::getCurrencyCode))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Moeda não encontrada: " + termo,
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return null;
        } else if (list.size() == 1) {
            return list.get(0).getCurrencyCode();
        } else {
            // múltiplas opções: mostra dialog de seleção
            String[] options = list.stream()
                    .map(c -> c.getCurrencyCode() + " - " +
                            c.getDisplayName(new Locale("pt","BR")))
                    .limit(10)
                    .toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(
                    this,
                    "Selecione:",
                    "Múltiplas moedas",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (sel == null) return null;
            return sel.split(" - ")[0];
        }
    }

    private static String normalize(String in) {
        return Normalizer.normalize(in, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CurrencyConverterUI::new);
    }
}
*/
// src/CurrencyConverterUI.java

import main.java.com.delnero.conversormoeda.http.ApiCliente;
import main.java.com.delnero.conversormoeda.service.ConverterMoedaLivreApi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CurrencyConverterUI extends JFrame {
    private static final Path FAVORITES_FILE = Paths.get("favorites.txt");

    private final ApiCliente api = new ApiCliente();

    // Top menu
    private final JComboBox<String> menuCombo;
    private final JButton btnGo;

    // Cards
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    // Free-conversion components
    private final JTextField tfBaseFree   = new JTextField(10);
    private final JTextField tfTargetFree = new JTextField(10);
    private final JTextField tfAmountFree = new JTextField("1.00", 8);
    private final JLabel    lblResultFree = new JLabel("Resultado: ---");
    private final JButton   btnConvertFree= new JButton("Converter");
    private final JButton   btnSaveFree   = new JButton("Salvar Favorito");

    // Preset-conversion components
    private final JLabel    lblPresetPair   = new JLabel();
    private final JTextField tfAmountPreset = new JTextField("1.00", 8);
    private final JLabel    lblResultPreset= new JLabel("Resultado: ---");
    private final JButton   btnConvertPreset = new JButton("Converter");
    private final JButton   btnSavePreset    = new JButton("Salvar Favorito");

    // Favorites list
    private final DefaultListModel<String> favModel = new DefaultListModel<>();
    private final JList<String> listFavs = new JList<>(favModel);
    private final JButton btnUseFav = new JButton("Usar Favorito");

    public CurrencyConverterUI() {
        super("Conversor de Moedas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // 1) Top panel: menu + Go
        String[] menus = {
                "0) Pesquisa livre",
                "1) USD → ARS",
                "2) ARS → USD",
                "3) USD → BRL",
                "4) BRL → USD",
                "5) USD → COP",
                "6) COP → USD",
                "7) Sair"
        };
        menuCombo = new JComboBox<>(menus);
        btnGo = new JButton("OK");
        JPanel top = new JPanel();
        top.add(new JLabel("Selecione:"));
        top.add(menuCombo);
        top.add(btnGo);
        add(top, BorderLayout.NORTH);

        // 2) Center: card panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(buildFreePanel(), "FREE");
        cardPanel.add(buildPresetPanel(), "PRESET");
        add(cardPanel, BorderLayout.CENTER);

        // 3) East: favorites list
        JPanel favPanel = new JPanel(new BorderLayout(4,4));
        favPanel.setBorder(BorderFactory.createTitledBorder("Favoritos"));
        favPanel.add(new JScrollPane(listFavs), BorderLayout.CENTER);
        favPanel.add(btnUseFav, BorderLayout.SOUTH);
        add(favPanel, BorderLayout.EAST);

        // 4) Actions
        btnGo.addActionListener(e -> updateCard());
        btnUseFav.addActionListener(e -> useFavorite());

        loadFavorites();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildFreePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4); c.anchor = GridBagConstraints.WEST;

        c.gridx=0; c.gridy=0; p.add(new JLabel("Moeda origem (código ou nome):"), c);
        c.gridx=1; p.add(tfBaseFree, c);
        c.gridx=0; c.gridy=1; p.add(new JLabel("Moeda destino (código ou nome):"), c);
        c.gridx=1; p.add(tfTargetFree, c);
        c.gridx=0; c.gridy=2; p.add(new JLabel("Valor:"), c);
        c.gridx=1; p.add(tfAmountFree, c);

        c.gridx=0; c.gridy=3; p.add(btnConvertFree, c);
        c.gridx=1; p.add(lblResultFree, c);

        c.gridx=0; c.gridy=4; p.add(btnSaveFree, c);

        btnConvertFree.addActionListener(e -> {
            String base   = resolveCurrency(tfBaseFree.getText());
            String target = resolveCurrency(tfTargetFree.getText());
            if (base==null||target==null) return;
            double amt = parseAmount(tfAmountFree.getText());
            if (Double.isNaN(amt)) return;
            doConvert(base, target, amt, lblResultFree);
        });
        btnSaveFree.addActionListener(e -> {
            saveFavorite(tfBaseFree.getText(), tfTargetFree.getText());
        });

        return p;
    }

    private JPanel buildPresetPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4); c.anchor = GridBagConstraints.WEST;

        c.gridx=0; c.gridy=0; p.add(new JLabel("Par:"), c);
        c.gridx=1; p.add(lblPresetPair, c);
        c.gridx=0; c.gridy=1; p.add(new JLabel("Valor:"), c);
        c.gridx=1; p.add(tfAmountPreset, c);

        c.gridx=0; c.gridy=2; p.add(btnConvertPreset, c);
        c.gridx=1; p.add(lblResultPreset, c);

        c.gridx=0; c.gridy=3; p.add(btnSavePreset, c);

        btnConvertPreset.addActionListener(e -> {
            String[] parts = lblPresetPair.getText().split("→");
            String base = parts[0].trim(), target = parts[1].trim();
            double amt = parseAmount(tfAmountPreset.getText());
            if (Double.isNaN(amt)) return;
            doConvert(base, target, amt, lblResultPreset);
        });
        btnSavePreset.addActionListener(e -> {
            String[] parts = lblPresetPair.getText().split("→");
            saveFavorite(parts[0].trim(), parts[1].trim());
        });

        return p;
    }

    private void updateCard() {
        int idx = menuCombo.getSelectedIndex();
        if (idx==7) System.exit(0);
        else if (idx==0) {
            cardLayout.show(cardPanel, "FREE");
        } else {
            String[][] presets = {
                    {}, {"USD","ARS"},{"ARS","USD"},
                    {"USD","BRL"},{"BRL","USD"},
                    {"USD","COP"},{"COP","USD"}
            };
            String[] p = presets[idx];
            lblPresetPair.setText(p[0]+" → "+p[1]);
            lblResultPreset.setText("Resultado: ---");
            tfAmountPreset.setText("1.00");
            cardLayout.show(cardPanel, "PRESET");
        }
        pack();
    }

    private void doConvert(String base, String target, double amount, JLabel out) {
        try {
            ConverterMoedaLivreApi res = api.buscaLivre(base, target, amount);
            out.setText(String.format(
                    Locale.US, "%.2f %s = %.2f %s",
                    amount, res.base_code(), res.conversion_result(), res.target_code()
            ));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String resolveCurrency(String termo) {
        String norm = normalize(termo).toUpperCase(Locale.ROOT);
        List<Currency> list = Currency.getAvailableCurrencies().stream()
                .filter(c -> {
                    String code = normalize(c.getCurrencyCode());
                    String pt   = normalize(c.getDisplayName(new Locale("pt","BR")));
                    String en   = normalize(c.getDisplayName(Locale.ENGLISH));
                    return code.contains(norm) ||
                            pt  .toUpperCase().contains(norm) ||
                            en  .toUpperCase().contains(norm);
                })
                .sorted((a,b)->a.getCurrencyCode().compareTo(b.getCurrencyCode()))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Moeda não encontrada: " + termo,
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return null;
        } else if (list.size()==1) {
            return list.get(0).getCurrencyCode();
        } else {
            String[] opts = list.stream()
                    .limit(10)
                    .map(c->c.getCurrencyCode()+" - "+c.getDisplayName(new Locale("pt","BR")))
                    .toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(
                    this, "Selecione:", "Múltiplas moedas",
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]
            );
            if (sel==null) return null;
            return sel.split(" - ")[0];
        }
    }

    private double parseAmount(String text) {
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return Double.NaN;
        }
    }

    private void saveFavorite(String baseInput, String targetInput) {
        String base = resolveCurrency(baseInput);
        String target = resolveCurrency(targetInput);
        if (base==null||target==null) return;
        String fav = base + "->" + target;
        if (!favModel.contains(fav)) {
            favModel.addElement(fav);
            try {
                Files.writeString(
                        FAVORITES_FILE,
                        fav + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void loadFavorites() {
        if (!Files.exists(FAVORITES_FILE)) return;
        try {
            Files.readAllLines(FAVORITES_FILE).forEach(line->{
                String l = line.trim();
                if (l.contains("->") && !favModel.contains(l)) {
                    favModel.addElement(l);
                }
            });
        } catch (IOException e) {
            // ignore
        }
    }

    private void useFavorite() {
        String fav = listFavs.getSelectedValue();
        if (fav == null) return;
        String[] p = fav.split("->");
        // força modo "Pesquisa livre"
        menuCombo.setSelectedIndex(0);
        updateCard();
        tfBaseFree.setText(p[0]);
        tfTargetFree.setText(p[1]);
        lblResultFree.setText("Resultado: ---");
        tfAmountFree.setText("1.00");
    }

    private static String normalize(String in) {
        return Normalizer.normalize(in, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CurrencyConverterUI::new);
    }
}


