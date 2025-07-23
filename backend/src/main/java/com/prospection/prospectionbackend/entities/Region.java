package com.prospection.prospectionbackend.entities;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nom;
    @Column(nullable = false, length = 100, unique = true)
    private String code;
    private LocalDateTime dateModification;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Supervision> supervisions;
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Utilisateur> utilisateurs;


}