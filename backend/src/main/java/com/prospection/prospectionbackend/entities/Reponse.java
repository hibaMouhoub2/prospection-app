package com.prospection.prospectionbackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reponses",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"question_id", "prospection_id"},
                name = "uk_question_prospection"
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"question", "prospection"}) // Éviter les références circulaires
public class Reponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation avec la question
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @NotNull(message = "La question est obligatoire")
    private Question question;

    // Relation avec la prospection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospection_id", nullable = false)
    @NotNull(message = "La prospection est obligatoire")
    private Prospection prospection;

    // Valeur de la réponse (stockée comme texte pour flexibilité)
    @Column(name = "valeur", columnDefinition = "TEXT")
    private String valeur;

    // Métadonnées
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column
    private LocalDateTime dateModification;

    // Constructeur utilitaire
    public Reponse(Question question, Prospection prospection, String valeur) {
        this.question = question;
        this.prospection = prospection;
        this.valeur = valeur;
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    /**
     * Vérifie si la réponse est vide
     * @return true si la valeur est null ou vide
     */
    public boolean estVide() {
        return valeur == null || valeur.trim().isEmpty();
    }

    /**
     * Valide la réponse selon le type de question
     * @return true si la réponse est valide
     */
    public boolean estValide() {
        if (question == null) {
            return false;
        }

        // Vérification obligatoire
        if (question.getObligatoire() && estVide()) {
            return false;
        }

        // Si non obligatoire et vide, c'est valide
        if (!question.getObligatoire() && estVide()) {
            return true;
        }

        // Validation selon le type de question
        switch (question.getType()) {
            case TEXT:

            case NUMBER:
                try {
                    if (valeur != null && !valeur.trim().isEmpty()) {
                        Integer.parseInt(valeur.trim());
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }

            case PHONE:
                if (valeur == null || valeur.trim().isEmpty()) {
                    return !question.getObligatoire();
                }
                // Validation du format téléphone marocain
                return valeur.trim().matches("^(06|07)\\d{8}$");



            case CHOICE:
                // Vérifier que la valeur correspond à une option valide
                return question.getOptions().stream()
                        .anyMatch(option -> option.getValeur().equals(valeur));

            case MULTIPLE_CHOICE:
                if (valeur == null || valeur.trim().isEmpty()) {
                    return !question.getObligatoire();
                }
                // Vérifier que toutes les valeurs sélectionnées sont valides
                String[] selections = valeur.split(",");
                for (String selection : selections) {
                    boolean optionValide = question.getOptions().stream()
                            .anyMatch(option -> option.getValeur().equals(selection.trim()));
                    if (!optionValide) {
                        return false;
                    }
                }
                return true;

            default:
                return true;
        }
    }

    /**
     * Formate la valeur pour l'affichage selon le type de question
     * @return La valeur formatée pour l'affichage
     */
    public String getValeurFormatee() {
        if (estVide()) {
            return "-";
        }

        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                // Afficher les choix multiples sur plusieurs lignes
                if (valeur.contains(",")) {
                    return String.join(", ", valeur.split(","));
                }
                return valeur;



            case PHONE:
                // Formater le numéro de téléphone
                if (valeur.length() == 10) {
                    return valeur.substring(0, 2) + " " +
                            valeur.substring(2, 4) + " " +
                            valeur.substring(4, 6) + " " +
                            valeur.substring(6, 8) + " " +
                            valeur.substring(8, 10);
                }
                return valeur;

            default:
                return valeur;
        }
    }

    /**
     * Récupère le libellé de la question associée
     * @return Le texte de la question
     */
    public String getLibelleQuestion() {
        return question != null ? question.getQuestion() : "";
    }
}