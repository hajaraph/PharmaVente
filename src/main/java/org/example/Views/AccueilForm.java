package org.example.Views;

import org.example.Controllers.DbConnexion;
import org.example.Models.Produit;
import org.example.Models.Stock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccueilForm extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ProduitForm.class.getName());
    private JComboBox<Produit> produitComboBox;
    private JTextField quantiteTextField;
    private DefaultTableModel tableModel;

    public AccueilForm() {
        initUI();
        loadProduits();
        loadVentes();
    }

    private void initUI() {
        setTitle("Interface Vente");
        setSize(1366, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Composants pour la sélection de produit et la quantité
        JLabel produitLabel = new JLabel("Produit:");
        produitComboBox = new JComboBox<>();
        produitComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Produit produit) {
                    setText(produit.toString());
                }
                return this;
            }
        });

        JLabel quantiteLabel = new JLabel("Quantité:");
        quantiteTextField = new JTextField();
        quantiteTextField.setPreferredSize(new Dimension(200, 30));

        JButton vendreButton = getVendreButton();

        JPanel ventePanel = new JPanel();
        ventePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
        ventePanel.add(produitLabel);
        ventePanel.add(produitComboBox);
        ventePanel.add(quantiteLabel);
        ventePanel.add(quantiteTextField);
        ventePanel.add(vendreButton);

        JPanel buttonPanel = getjPanel();

        // Ajouter la colonne "Action"
        String[] columnNames = {"Produit", "Quantité Vendue", "Prix Total", "Date Vente", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Les colonnes "Action" sont modifiables pour les boutons
                return column == 4;
            }
        };
        JTable venteTable = new JTable(tableModel);
        venteTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        venteTable.getColumn("Action").setCellEditor(new ButtonEditor());
        JScrollPane scrollPane = new JScrollPane(venteTable);

        setLayout(new BorderLayout());
        add(ventePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel getjPanel() {
        JButton produitButton = new JButton("Produit");
        produitButton.addActionListener(e -> {
            ProduitForm produitForm = new ProduitForm();
            AccueilForm.this.setVisible(false);
            produitForm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    AccueilForm.this.setVisible(true);
                }
            });
        });

        JButton stockButton = new JButton("Ouvrir Formulaire Stock");
        stockButton.addActionListener(e -> {
            StockForm stockForm = new StockForm();
            this.setVisible(false);
            stockForm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    AccueilForm.this.setVisible(true);
                }
            });
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(produitButton);
        buttonPanel.add(stockButton);
        return buttonPanel;
    }

    private JButton getVendreButton() {
        JButton vendreButton = new JButton("Vendre");

        vendreButton.addActionListener(e -> {
            Produit produit = (Produit) produitComboBox.getSelectedItem();
            int quantiteVendue;
            try {
                quantiteVendue = Integer.parseInt(quantiteTextField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer une quantité valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (produit != null) {
                try (Connection connection = DbConnexion.getConnection()) {
                    if (connection != null) {
                        // Vérifier si le stock est suffisant en utilisant le nom du produit
                        String selectSql = "SELECT stock.idProduit, stock.quantite FROM stock " +
                                "INNER JOIN produit ON stock.idProduit = produit.idProduit " +
                                "WHERE produit.nomProduit = ?";
                        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                        selectStatement.setString(1, produit.getNomProduit());
                        ResultSet resultSet = selectStatement.executeQuery();

                        if (resultSet.next()) {
                            int idProduit = resultSet.getInt("stock.idProduit");
                            int quantiteEnStock = resultSet.getInt("stock.quantite");

                            if (quantiteVendue <= quantiteEnStock) {
                                // Mettre à jour le stock après la vente
                                int nouveauStock = quantiteEnStock - quantiteVendue;
                                String updateSql = "UPDATE stock SET quantite = ? WHERE idProduit = ?";
                                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                                updateStatement.setInt(1, nouveauStock);
                                updateStatement.setInt(2, idProduit);
                                updateStatement.executeUpdate();

                                // Calculer le prix total de la vente
                                double prixTotal = produit.getPrixProduit() * quantiteVendue;

                                // Insérer la vente dans la base de données
                                String insertSql = "INSERT INTO vente (idProduit, quantiteVendue, prixTotal, dateVente) VALUES (?, ?, ?, ?)";
                                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                                insertStatement.setInt(1, idProduit); // Utiliser l'ID du produit
                                insertStatement.setInt(2, quantiteVendue);
                                insertStatement.setDouble(3, prixTotal);
                                insertStatement.setObject(4, LocalDateTime.now()); // Utiliser LocalDateTime comme SQL timestamp
                                insertStatement.executeUpdate();

                                // Ajouter la vente au tableau
                                tableModel.addRow(new Object[]{produit.getNomProduit(), quantiteVendue, prixTotal, LocalDateTime.now(), new JButton("Supprimer")});

                                // Réinitialiser le champ de quantité
                                quantiteTextField.setText("");

                                JOptionPane.showMessageDialog(this, "Vente effectuée avec succès !");
                            } else {
                                JOptionPane.showMessageDialog(this, "Quantité insuffisante en stock !", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error loading data", ex);
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

                List<Stock> stockList = new ArrayList<>();

                while (resultSet.next()) {
                    int idProduit = resultSet.getInt("idProduit");
                    String nomProduit = resultSet.getString("nomProduit");
                    double prixProduit = resultSet.getDouble("prixProduit");

                    Produit produit = new Produit(idProduit, nomProduit, prixProduit);
                    produitComboBox.addItem(produit);

                    // Charger le stock associé à chaque produit
                    String selectStockSql = "SELECT quantite FROM stock WHERE idProduit=?";
                    PreparedStatement stockStatement = connection.prepareStatement(selectStockSql);
                    stockStatement.setInt(1, idProduit);
                    ResultSet stockResultSet = stockStatement.executeQuery();
                    if (stockResultSet.next()) {
                        int quantite = stockResultSet.getInt("quantite");
                        stockList.add(new Stock(quantite, produit));
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading data", ex);
        }
    }

    private void loadVentes() {
        try (Connection connection = DbConnexion.getConnection()) {
            if (connection != null) {
                String selectSql = "SELECT v.idVente, p.nomProduit, v.quantiteVendue, v.prixTotal, v.dateVente " +
                        "FROM vente v INNER JOIN produit p ON v.idProduit = p.idProduit";
                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                ResultSet resultSet = selectStatement.executeQuery();

                // Vider le modèle de table avant d'ajouter de nouvelles données
                tableModel.setRowCount(0);

                while (resultSet.next()) {
                    int idVente = resultSet.getInt("idVente");
                    String nomProduit = resultSet.getString("nomProduit");
                    int quantiteVendue = resultSet.getInt("quantiteVendue");
                    double prixTotal = resultSet.getDouble("prixTotal");
                    LocalDateTime dateVente = resultSet.getObject("dateVente", LocalDateTime.class);

                    // Créer un bouton "Supprimer"
                    JButton deleteButton = new JButton("Supprimer");
                    deleteButton.addActionListener(e -> {
                        // Action pour supprimer la vente
                        deleteVente(idVente);
                    });

                    // Ajouter les données au modèle de table avec le bouton
                    tableModel.addRow(new Object[]{
                            nomProduit,
                            quantiteVendue,
                            prixTotal,
                            dateVente,
                            deleteButton
                    });
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error loading data", ex);
        }
    }

    private void deleteVente(int idVente) {
        int confirm = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir supprimer cette vente ?", "Confirmer Suppression", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DbConnexion.getConnection()) {
                if (connection != null) {
                    // Supprimer la vente de la base de données
                    String deleteSql = "DELETE FROM vente WHERE idVente = ?";
                    PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
                    deleteStatement.setInt(1, idVente);
                    deleteStatement.executeUpdate();

                    // Recharger les ventes pour mettre à jour la table
                    loadVentes();

                    JOptionPane.showMessageDialog(this, "Vente supprimée avec succès !");
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error loading data", ex);
            }
        }
    }

    // Classe pour rendre le bouton dans la cellule
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JButton) {
                return (JButton) value;
            }
            return this;
        }
    }

    // Classe pour éditer le bouton dans la cellule
    static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;

        public ButtonEditor() {
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof JButton) {
                button = (JButton) value;
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button;
        }
    }
}
