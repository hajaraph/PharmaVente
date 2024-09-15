package org.example.Views;

import org.example.Controllers.DbConnexion;
import org.example.Models.Produit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockForm extends JFrame {

    private JComboBox<Produit> produitComboBox;
    private JTextField quantiteTextField;

    public StockForm() {
        initUI();
        loadProduits();
    }

    private void initUI() {
        setTitle("Enregistrer un Produit dans le Stock");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Création des composants
        JLabel produitLabel = new JLabel("Produit:");
        produitComboBox = new JComboBox<>();

        JLabel quantiteLabel = new JLabel("Quantité:");
        quantiteTextField = new JTextField();
        quantiteTextField.setPreferredSize(new Dimension(200, 30));

        JButton enregistrerButton = new JButton("Enregistrer");
        enregistrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enregistrerProduitStock();
            }
        });

        // Panneau principal
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
        panel.add(produitLabel);
        panel.add(produitComboBox);
        panel.add(quantiteLabel);
        panel.add(quantiteTextField);
        panel.add(enregistrerButton);

        // Ajouter le panneau à la fenêtre
        add(panel);

        setVisible(true);
    }

    private void loadProduits() {
        // Charger les produits depuis la base de données et les ajouter au ComboBox
        try (Connection connection = DbConnexion.getConnection()) {
            if (connection != null) {
                String selectSql = "SELECT * FROM produit";
                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                ResultSet resultSet = selectStatement.executeQuery();

                while (resultSet.next()) {
                    int idProduit = resultSet.getInt("idProduit");
                    String nomProduit = resultSet.getString("nomProduit");
                    double prixProduit = resultSet.getDouble("prixProduit");

                    Produit produit = new Produit(idProduit, nomProduit, prixProduit);
                    produitComboBox.addItem(produit);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void enregistrerProduitStock() {
        Produit produit = (Produit) produitComboBox.getSelectedItem();
        int quantite = Integer.parseInt(quantiteTextField.getText());

        if (produit != null) {
            try (Connection connection = DbConnexion.getConnection()) {
                if (connection != null) {
                    // Vérifier si le produit est déjà dans le stock
                    String selectSql = "SELECT * FROM stock WHERE idProduit = ?";
                    PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                    selectStatement.setInt(1, produit.getIdProduit());
                    ResultSet resultSet = selectStatement.executeQuery();

                    if (resultSet.next()) {
                        // Mettre à jour la quantité si le produit existe déjà dans le stock
                        int quantiteExistante = resultSet.getInt("quantite");
                        int nouvelleQuantite = quantiteExistante + quantite;

                        String updateSql = "UPDATE stock SET quantite = ? WHERE idProduit = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                        updateStatement.setInt(1, nouvelleQuantite);
                        updateStatement.setInt(2, produit.getIdProduit());
                        updateStatement.executeUpdate();

                        JOptionPane.showMessageDialog(this, "Stock mis à jour avec succès !");
                    } else {
                        // Ajouter un nouveau produit au stock
                        String insertSql = "INSERT INTO stock (idProduit, quantite) VALUES (?, ?)";
                        PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                        insertStatement.setInt(1, produit.getIdProduit());
                        insertStatement.setInt(2, quantite);
                        insertStatement.executeUpdate();

                        JOptionPane.showMessageDialog(this, "Produit ajouté au stock avec succès !");
                    }

                    // Réinitialiser les champs
                    quantiteTextField.setText("");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
