package com.prospection.prospectionbackend.controllers;

import com.prospection.prospectionbackend.dto.AuthResponse;
import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.enums.Role;
import com.prospection.prospectionbackend.services.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class RegistrationController {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        try {
            // Validation du rôle
            Role role;
            try {
                role = request.getRole() != null ? request.getRole() : Role.AGENT;
            } catch (IllegalArgumentException e) {
                AuthResponse response = AuthResponse.error("Rôle invalide: " + request.getRole());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Utilisateur utilisateur = userRegistrationService.createUser(
                    request.getNom(),
                    request.getPrenom(),
                    request.getEmail(),
                    request.getTelephone() != null ? request.getTelephone() : "0000000000", // Valeur par défaut
                    request.getMotDePasse(),
                    role,
                    request.getRegionId(),
                    request.getSupervisionId(),
                    request.getBrancheId()
            );

            AuthResponse response = AuthResponse.success(
                    "Utilisateur créé avec succès - Rôle: " + role.getDisplayName(),
                    null, // Pas de token lors de l'enregistrement
                    null,
                    null, null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            AuthResponse response = AuthResponse.error("Erreur lors de la création: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    // DTO pour la requête d'enregistrement
    public static class RegistrationRequest {
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String motDePasse;
        private Role role;
        private Long regionId;      // NOUVEAU
        private Long supervisionId; // NOUVEAU
        private Long brancheId;

        // Getters et setters
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getMotDePasse() { return motDePasse; }
        public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public Long getRegionId() { return regionId; }
        public void setRegionId(Long regionId) { this.regionId = regionId; }
        public Long getSupervisionId() { return supervisionId; }
        public void setSupervisionId(Long supervisionId) { this.supervisionId = supervisionId; }
        public Long getBrancheId() { return brancheId; }
        public void setBrancheId(Long brancheId) { this.brancheId = brancheId; }
    }
}