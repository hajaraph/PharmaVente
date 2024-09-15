package org.example;

import org.example.Views.AccueilForm;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Créer et afficher le formulaire d'accueil
        SwingUtilities.invokeLater(AccueilForm::new);
    }
}