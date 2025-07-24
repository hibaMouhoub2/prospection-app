package com.prospection.prospectionbackend.services;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.Role;
import com.prospection.prospectionbackend.repositories.BrancheRepository;
import com.prospection.prospectionbackend.repositories.RegionRepository;
import com.prospection.prospectionbackend.repositories.SupervisionRepository;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Transactional
public class UserRegistrationService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BrancheRepository brancheRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;
    @Autowired
    private RegionRepository regionRepository;


    public Utilisateur createUser(String nom, String prenom, String email, String telephone,
                                  String motDePasse, Role role, Long regionId, Long supervisionId, Long brancheId) {

        if (utilisateurRepository.existsByEmail(email)) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setTelephone(telephone != null ? telephone : "0000000000");
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateur.setDateCreation(LocalDateTime.now());

        // Assignation selon le rôle
        switch (role) {
            case AGENT:
            case CHEF_BRANCHE:
                if (brancheId != null) {
                    brancheRepository.findById(brancheId).ifPresent(branche -> {
                        utilisateur.setBranche(branche);
                        utilisateur.setSupervision(branche.getSupervision());
                        utilisateur.setRegion(branche.getSupervision().getRegion());
                    });
                }
                break;
            case SUPERVISEUR:
                if (supervisionId != null) {
                    supervisionRepository.findById(supervisionId).ifPresent(supervision -> {
                        utilisateur.setSupervision(supervision);
                        utilisateur.setRegion(supervision.getRegion());
                    });
                }
                break;
            case CHEF_ANIMATION_REGIONAL:
                if (regionId != null) {
                    regionRepository.findById(regionId).ifPresent(utilisateur::setRegion);
                }
                break;
        }

        return utilisateurRepository.save(utilisateur);
    }
}