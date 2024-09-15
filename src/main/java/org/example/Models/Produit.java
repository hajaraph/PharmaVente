package org.example.Models;

public class Produit {
    private int idProduit;
    private String nomProduit;
    private double prixProduit;

    // Constructeur
    public Produit(int idProduit, String nomProduit, double prixProduit) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.prixProduit = prixProduit;
    }

    public Produit(){}

    // Constructeur sans ID pour les nouvelles insertions
    public Produit(String nomProduit, double prixProduit) {
        this.nomProduit = nomProduit;
        this.prixProduit = prixProduit;
    }

    // Getters et Setters
    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public double getPrixProduit() {
        return prixProduit;
    }

    public void setPrixProduit(double prixProduit) {
        this.prixProduit = prixProduit;
    }

    @Override
    public String toString() {
        return nomProduit + " - " + prixProduit + "€"; // Format: Nom - Prix€
    }
}
