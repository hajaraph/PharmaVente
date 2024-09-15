package org.example.Views;

import org.example.Controllers.DbConnexion;
import org.example.Models.Produit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProduitForm extends JFrame {

    private JTextField nomTextField;
    private JTextField prixTextField;
    private JTable produitTable;
    private DefaultTableModel tableModel;

    public ProduitForm() {
        initUI();
    }

    private void initUI() {
        setTitle("Interface Produit");
        setSize(1366, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Création du formulaire de saisie
        JLabel nomLabel = new JLabel("Nom du Produit:");
        nomTextField = new JTextField();
        nomTextField.setPreferredSize(new Dimension(200, 30));

        JLabel prixLabel = new JLabel("Prix:");
        prixTextField = new JTextField();
        prixTextField.setPreferredSize(new Dimension(200, 30));

        JButton submitButton = getSubmitButton();

        // Panneau de saisie
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
        panel.add(nomLabel);
        panel.add(nomTextField);
        panel.add(prixLabel);
        panel.add(prixTextField);
        panel.add(submitButton);

        // Tableau de produits
        String[] columnNames = {"ID", "Nom", "Prix", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        produitTable = new JTable(tableModel);

        // Ajouter des boutons dans la colonne "Action"
        produitTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        produitTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Panneau du tableau
        JScrollPane scrollPane = new JScrollPane(produitTable);

        // Layout principal
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Charger les données existantes
        loadData();

        setVisible(true);
    }

    private JButton getSubmitButton() {
        JButton submitButton = new JButton("Soumettre");

        // Action du bouton de soumission
        submitButton.addActionListener(e -> {
            String nomProduit = nomTextField.getText();
            double prix = Double.parseDouble(prixTextField.getText());

            Produit produit = new Produit(nomProduit, prix);
            System.out.println("Produit créé: " + produit.nomProduit() + ", Prix: " + produit.prix());

            // Enregistrer dans la base de données
            try (Connection connection = DbConnexion.getConnection()) {
                if (connection != null) {
                    // Vérifier si le produit existe déjà
                    String selectSql = "SELECT * FROM produit WHERE nomProduit=?";
                    PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                    selectStatement.setString(1, produit.nomProduit());
                    ResultSet resultSet = selectStatement.executeQuery();

                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(this, "Le produit existe déjà dans la base de données.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Insérer le produit
                        String insertSql = "INSERT INTO produit (nomProduit, prixProduit) VALUES (?, ?)";
                        PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                        insertStatement.setString(1, produit.nomProduit());
                        insertStatement.setDouble(2, produit.prix());
                        insertStatement.executeUpdate();
                        System.out.println("Produit inséré dans la base de données.");

                        // Rafraîchir les données de la table après insertion
                        loadData();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        return submitButton;
    }

    private void loadData() {
        try (Connection connection = DbConnexion.getConnection()) {
            if (connection != null) {
                String selectSql = "SELECT * FROM produit";
                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                ResultSet resultSet = selectStatement.executeQuery();

                // Effacer les lignes existantes
                tableModel.setRowCount(0);

                while (resultSet.next()) {
                    int id = resultSet.getInt("idProduit");
                    String nom = resultSet.getString("nomProduit");
                    double prix = resultSet.getDouble("prixProduit");

                    // Ajouter les données dans le modèle de table
                    tableModel.addRow(new Object[]{id, nom, prix, "Modifier/Supprimer"});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Renderer pour les boutons
    static class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setLayout(new FlowLayout());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton editButton = new JButton("Modifier");
            JButton deleteButton = new JButton("Supprimer");
            add(editButton);
            add(deleteButton);
            return this;
        }
    }

    // Editor pour les boutons
    class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout());
            JButton editButton = new JButton("Modifier");
            JButton deleteButton = new JButton("Supprimer");

            panel.add(editButton);
            panel.add(deleteButton);

            // Action pour le bouton Modifier
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Logique pour modifier le produit
                    int row = produitTable.getSelectedRow();
                    String nom = (String) produitTable.getValueAt(row, 1);
                    double prix = (double) produitTable.getValueAt(row, 2);
                    System.out.println("Modifier le produit: " + nom + ", Prix: " + prix);
                }
            });

            // Action pour le bouton Supprimer
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int row = produitTable.getSelectedRow();
                    int id = (int) produitTable.getValueAt(row, 0);

                    // Supprimer le produit de la base de données
                    try (Connection connection = DbConnexion.getConnection()) {
                        if (connection != null) {
                            String deleteSql = "DELETE FROM produit WHERE idProduit=?";
                            PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                            deleteStatement.setInt(1, id);
                            deleteStatement.executeUpdate();
                            System.out.println("Produit supprimé de la base de données.");

                            // Rafraîchir les données après suppression
                            loadData();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return panel;
        }
    }
}
