package org.example.Models;

public class Stock {
    private int idStock;
    private int quantite;
    private Produit produit;

    public Stock(int quantite, Produit produit) {
        this.quantite = quantite;
        this.produit = produit;
        this.idStock = idStock;
    }

    // Getters et setters
    public int getQuantite() {
        return quantite;
    }

    public int getIdStock() {
        return idStock;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public void setIdStock(int idStock) {
        this.idStock = idStock;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "quantite=" + quantite +
                ", produit=" + produit.nomProduit() +
                '}';
    }
}
