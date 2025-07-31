package com.prospection.prospectionbackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "relances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"prospection", "agent"})
public class Relance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospection_id", nullable = false)
    @NotNull(message = "La prospection est obligatoire")
    private Prospection prospection;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    @NotNull(message = "L'agent est obligatoire")
    private Utilisateur agent;


    @Column(nullable = false)
    private LocalDateTime dateRelance;


    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String commentaire;


    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TypeRelance typeRelance;


    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;


    public Relance(Prospection prospection, Utilisateur agent, String commentaire) {
        this.prospection = prospection;
        this.agent = agent;
        this.commentaire = commentaire;
        this.dateRelance = LocalDateTime.now();
        this.typeRelance = TypeRelance.APPEL_TELEPHONIQUE; // Par défaut
    }

    public Relance(Prospection prospection, Utilisateur agent, String commentaire, TypeRelance typeRelance) {
        this.prospection = prospection;
        this.agent = agent;
        this.commentaire = commentaire;
        this.dateRelance = LocalDateTime.now();
        this.typeRelance = typeRelance;
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateRelance == null) {
            dateRelance = LocalDateTime.now();
        }
        if (typeRelance == null) {
            typeRelance = TypeRelance.APPEL_TELEPHONIQUE;
        }
    }


    public enum TypeRelance {
        APPEL_TELEPHONIQUE("Appel téléphonique", "📞");


        private final String displayName;
        private final String icon;

        TypeRelance(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }

        public String getDisplayWithIcon() {
            return icon + " " + displayName;
        }
    }


    public String getNomCompletAgent() {
        if (agent == null) {
            return "Agent inconnu";
        }
        return agent.getPrenom() + " " + agent.getNom();
    }

    /**
     * Vérifie si cette relance est récente (moins de 24h)
     * @return true si la relance date de moins de 24h
     */
    public boolean estRecente() {
        return dateRelance != null &&
                dateRelance.isAfter(LocalDateTime.now().minusDays(1));
    }

    /**
     * Calcule le nombre de jours depuis cette relance
     * @return Nombre de jours écoulés
     */
    public long joursDepuisRelance() {
        if (dateRelance == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dateRelance.toLocalDate(), LocalDateTime.now().toLocalDate());
    }
}