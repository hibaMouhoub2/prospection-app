package com.prospection.prospectionbackend.entities;

import com.prospection.prospectionbackend.enums.StatutProspection;
import com.prospection.prospectionbackend.enums.TypeProspection;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prospections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"reponses", "relances"})
public class Prospection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    @Column
    private LocalDateTime dateModification;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le type de prospection est obligatoire")
    @Column(nullable = false)
    private TypeProspection typeProspection;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    @Column(nullable = false)
    private StatutProspection statut = StatutProspection.NOUVEAU;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id", nullable = false)
    @NotNull(message = "Le créateur est obligatoire")
    private Utilisateur createur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_assigne_id")
    private Utilisateur agentAssigne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branche_id")
    private Branche branche;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervision_id")
    private Supervision supervision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(length = 1000)
    private String commentaire;

    @Column
    private LocalDateTime dateDerniereRelance;

    @Column
    private LocalDateTime dateConversion;

    @OneToMany(mappedBy = "prospection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Reponse> reponses = new ArrayList<>();

    @OneToMany(mappedBy = "prospection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Relance> relances = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutProspection.NOUVEAU;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public void extraireInfosProspect() {
        if (reponses == null || reponses.isEmpty()) {
            return;
        }

        for (Reponse reponse : reponses) {
            if (reponse.getQuestion() == null || reponse.getValeur() == null) {
                continue;
            }

            String questionText = reponse.getQuestion().getQuestion().toLowerCase();
            String valeur = reponse.getValeur().trim();

            if (valeur.isEmpty()) {
                continue;
            }


        }
    }
}
