package org.example.Views;

import org.example.Controllers.DbConnexion;
import org.example.Models.Produit;
import org.example.Models.Stock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccueilForm extends JFrame {

    private JComboBox<Produit> produitComboBox;
    private JTextField quantiteTextField;
    private JTable venteTable;
    private DefaultTableModel tableModel;
    private List<Stock> stockList;

    public AccueilForm() {
        initUI();
        loadProduits();
    }

    private void initUI() {
        setTitle("Interface Vente");
        setSize(1366, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Composants pour la sélection de produit et la quantité
        JLabel produitLabel = new JLabel("Produit:");
        produitComboBox = new JComboBox<>();

        JLabel quantiteLabel = new JLabel("Quantité:");
        quantiteTextField = new JTextField();
        quantiteTextField.setPreferredSize(new Dimension(200, 30));

        JButton vendreButton = getVendreButton();

        // Panneau pour la saisie des ventes
        JPanel ventePanel = new JPanel();
        ventePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
        ventePanel.add(produitLabel);
        ventePanel.add(produitComboBox);
        ventePanel.add(quantiteLabel);
        ventePanel.add(quantiteTextField);
        ventePanel.add(vendreButton);

        // Ajouter des boutons pour ouvrir les formulaires Produit et Stock
        JButton produitButton = new JButton("Ouvrir Formulaire Produit");
        produitButton.addActionListener(e -> {
            ProduitForm produitForm = new ProduitForm(); // Ouvrir l'interface Produit
            this.setVisible(false); // Cacher AccueilForm
            produitForm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    AccueilForm.this.setVisible(true); // Réafficher AccueilForm quand ProduitForm est fermé
                }
            });
        });

//        JButton stockButton = new JButton("Ouvrir Formulaire Stock");
//        stockButton.addActionListener(e -> {
//            StockForm stockForm = new StockForm(); // Ouvrir l'interface Stock
//            this.setVisible(false); // Cacher AccueilForm
//            stockForm.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    AccueilForm.this.setVisible(true); // Réafficher AccueilForm quand StockForm est fermé
//                }
//            });
//        });

//        JPanel buttonPanel = new JPanel();
//        buttonPanel.add(produitButton);
//        buttonPanel.add(stockButton);

        // Tableau des ventes
        String[] columnNames = {"Produit", "Quantité Vendue", "Prix Total"};
        tableModel = new DefaultTableModel(columnNames, 0);
        venteTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(venteTable);

        // Layout principal
        setLayout(new BorderLayout());
        add(ventePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
//        add(buttonPanel, BorderLayout.SOUTH); // Ajouter le panneau avec les boutons en bas

        setVisible(true);
    }

    private JButton getVendreButton() {
        JButton vendreButton = new JButton("Vendre");

        vendreButton.addActionListener(e -> {
            Produit produit = (Produit) produitComboBox.getSelectedItem();
            int quantiteVendue = Integer.parseInt(quantiteTextField.getText());

            if (produit != null) {
                try (Connection connection = DbConnexion.getConnection()) {
                    if (connection != null) {
                        // Vérifier si le stock est suffisant
                        String selectSql = "SELECT quantite FROM stock WHERE nomProduit = ?";
                        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                        selectStatement.setString(1, produit.nomProduit());
                        ResultSet resultSet = selectStatement.executeQuery();

                        if (resultSet.next()) {
                            int quantiteEnStock = resultSet.getInt("quantite");

                            if (quantiteVendue <= quantiteEnStock) {
                                // Mettre à jour le stock après la vente
                                int nouveauStock = quantiteEnStock - quantiteVendue;
                                String updateSql = "UPDATE stock SET quantite = ? WHERE nomProduit = ?";
                                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                                updateStatement.setInt(1, nouveauStock);
                                updateStatement.setString(2, produit.nomProduit());
                                updateStatement.executeUpdate();

                                // Calculer le prix total de la vente
                                double prixTotal = produit.prix() * quantiteVendue;

                                // Ajouter la vente au tableau
                                tableModel.addRow(new Object[]{produit.nomProduit(), quantiteVendue, prixTotal});

                                // Réinitialiser le champ de quantité
                                quantiteTextField.setText("");

                                JOptionPane.showMessageDialog(this, "Vente effectuée avec succès !");
                            } else {
                                JOptionPane.showMessageDialog(this, "Quantité insuffisante en stock !", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return vendreButton;
    }

    private void loadProduits() {
        try (Connection connection = DbConnexion.getConnection()) {
            if (connection != null) {
                String selectSql = "SELECT * FROM produit";
                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                ResultSet resultSet = selectStatement.executeQuery();

                stockList = new ArrayList<>();

                while (resultSet.next()) {
                    String nomProduit = resultSet.getString("nomProduit");
                    double prixProduit = resultSet.getDouble("prixProduit");

                    Produit produit = new Produit(nomProduit, prixProduit);
                    produitComboBox.addItem(produit);

                    // Charger le stock associé à chaque produit
                    String selectStockSql = "SELECT quantite FROM stock WHERE nomProduit=?";
                    PreparedStatement stockStatement = connection.prepareStatement(selectStockSql);
                    stockStatement.setString(1, nomProduit);
                    ResultSet stockResultSet = stockStatement.executeQuery();
                    if (stockResultSet.next()) {
                        int quantite = stockResultSet.getInt("quantite");
                        stockList.add(new Stock(quantite, produit));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
