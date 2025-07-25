package com.prospection.prospectionbackend.entities;


import com.prospection.prospectionbackend.enums.QuestionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@ToString(exclude = {"options"})
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "La question est obligatoire")
    @Column(nullable = false, length = 500)
    private String question;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le type de la question est obligatoire")
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    private Integer ordre;
    @Column(nullable = false)
    private Boolean actif = true;

    @Column(nullable = false)
    private Boolean obligatoire = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @NotNull(message = "Le créateur est obligatoire")
    @Column(nullable = false)
    private Long createurId;


    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("ordreOption ASC")
    private List<QuestionOption> options = new ArrayList<>();


    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(QuestionOption option) {
        options.remove(option);
        option.setQuestion(null);
    }

    public boolean hasOptions() {
        return type == QuestionType.CHOICE || type == QuestionType.MULTIPLE_CHOICE;
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }


}
