package com.prospection.prospectionbackend.services;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.Role;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service d'enregistrement des utilisateurs
 * Responsabilité: Créer de nouveaux comptes avec mot de passe haché
 */
@Service
@Transactional
public class UserRegistrationService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Créer un nouvel utilisateur avec mot de passe haché
     */
    public Utilisateur createUser(String nom, String prenom, String email,
                                  String telephone, String motDePasse, Role role) {

        System.out.println("=== CREATION UTILISATEUR ===");
        System.out.println("Email: " + email);
        System.out.println("Nom: " + nom + " " + prenom);

        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(email)) {
            System.out.println("Email déjà existant: " + email);
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Créer le nouvel utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setTelephone(telephone);

        // POINT CRUCIAL : Hacher le mot de passe
        String motDePasseHache = passwordEncoder.encode(motDePasse);
        utilisateur.setMotDePasse(motDePasseHache);

        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateur.setDateCreation(LocalDateTime.now());

        // Sauvegarder en base
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        System.out.println("Utilisateur créé avec succès");
        System.out.println("ID: " + savedUser.getId());
        System.out.println("Mot de passe original: " + motDePasse);
        System.out.println("Mot de passe haché: " + motDePasseHache);
        System.out.println("Longueur du hash: " + motDePasseHache.length());

        return savedUser;
    }
}