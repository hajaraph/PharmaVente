package org.example.Models;

import java.time.LocalDateTime;

public class Vente {
    private int idVente;
    private Produit produit;
    private int quantiteVendue;
    private double prixTotal;
    private LocalDateTime dateVente;

    // Constructeur
    public Vente(int idVente, Produit produit, int quantiteVendue, double prixTotal, LocalDateTime dateVente) {
        this.idVente = idVente;
        this.produit = produit;
        this.quantiteVendue = quantiteVendue;
        this.prixTotal = prixTotal;
        this.dateVente = dateVente;
    }

    // Getters et setters
    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantiteVendue() {
        return quantiteVendue;
    }

    public void setQuantiteVendue(int quantiteVendue) {
        this.quantiteVendue = quantiteVendue;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    // Méthode pour calculer le prix total de la vente
    public void calculerPrixTotal() {
        this.prixTotal = this.quantiteVendue * this.produit.prix();
    }

    // toString() pour afficher les informations de la vente
    @Override
    public String toString() {
        return "Vente{" +
                "idVente=" + idVente +
                ", produit=" + produit.nomProduit() +
                ", quantiteVendue=" + quantiteVendue +
                ", prixTotal=" + prixTotal +
                ", dateVente=" + dateVente +
                '}';
    }
}
