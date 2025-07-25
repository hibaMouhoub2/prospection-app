package com.prospection.prospectionbackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "question_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"question"}) // Éviter les références circulaires
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La valeur de l'option est obligatoire")
    @Column(nullable = false, length = 255)
    private String valeur;

    @NotNull(message = "L'ordre de l'option est obligatoire")
    @Column(nullable = false)
    private Integer ordreOption;

    // Relation avec la question parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @NotNull(message = "La question est obligatoire")
    private Question question;

    // Constructeur utilitaire
    public QuestionOption(String valeur, Integer ordreOption) {
        this.valeur = valeur;
        this.ordreOption = ordreOption;
    }
}