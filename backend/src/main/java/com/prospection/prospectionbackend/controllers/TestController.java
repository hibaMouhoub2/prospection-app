package com.prospection.prospectionbackend.controllers;

import com.prospection.prospectionbackend.entities.Utilisateur;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de test pour vérifier l'authentification JWT
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TestController {

    /**
     * Endpoint protégé pour tester le JWT
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> testProtected() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof Utilisateur) {
            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();

            response.put("success", true);
            response.put("message", "Accès autorisé !");
            response.put("user", Map.of(
                    "email", utilisateur.getEmail(),
                    "nom", utilisateur.getNom(),
                    "prenom", utilisateur.getPrenom(),
                    "role", utilisateur.getRole().getDisplayName()
            ));

            System.out.println("✅ Accès autorisé pour: " + utilisateur.getEmail());
        } else {
            response.put("success", false);
            response.put("message", "Authentification manquante");

            System.out.println("❌ Pas d'authentification trouvée");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint public pour comparaison
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> testPublic() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Endpoint public accessible à tous");

        System.out.println("ℹ️ Accès endpoint public");

        return ResponseEntity.ok(response);
    }
}