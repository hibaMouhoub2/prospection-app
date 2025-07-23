package com.prospection.prospectionbackend.entities;


import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "supervisions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Supervision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column( nullable = false, length = 100)
    private String nom;
    @Column( nullable = false, length = 100, unique = true)
    private String code;
    private LocalDateTime dateModification;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @NotNull
    private Region region;

    @OneToMany(mappedBy = "supervision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Branche> branches;

    @OneToMany(mappedBy = "supervision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Utilisateur> utilisateurs;
}
