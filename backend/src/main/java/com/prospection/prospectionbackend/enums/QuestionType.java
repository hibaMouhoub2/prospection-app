package com.prospection.prospectionbackend.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum QuestionType {
    TEXT("Texte libre", "Réponse sous forme de texte"),
    NUMBER("Nombre entier", "Réponse numérique"),
    PHONE("Numéro de téléphone", "Numéro de téléphone"),
    CHOICE("Choix unique", "Une seule réponse possible parmi les options"),
    MULTIPLE_CHOICE("Choix multiples", "Plusieurs réponses possibles parmi les options");

    private final String displayName;
    private final String description;

    QuestionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }


    public boolean requiresOptions() {
        return this == CHOICE || this == MULTIPLE_CHOICE;
    }


    public String getValidationPattern() {
        switch (this) {
            case PHONE:
                return "^(06|07)\\d{8}$"; // Format marocain : 06/07 + 8 chiffres
            case NUMBER:
                return "^\\d+$"; // Entiers uniquement
            default:
                return null;
        }
    }


    public String getValidationMessage() {
        switch (this) {
            case PHONE:
                return "Le numéro doit commencer par 06 ou 07 et contenir 10 chiffres";
            case NUMBER:
                return "Veuillez saisir un nombre entier";
            default:
                return "Format invalide";
        }
    }
}