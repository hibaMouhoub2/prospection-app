package com.prospection.prospectionbackend.controllers;


import com.prospection.prospectionbackend.dto.AuthResponse;
import com.prospection.prospectionbackend.dto.LoginRequest;
import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.services.AuthService;
import com.prospection.prospectionbackend.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Tentative d'authentification
            Map<String, Object> authResult = authService.login(loginRequest.getEmail(), loginRequest.getMotDePasse());

            // Extraction des données
            String token = (String) authResult.get("token");
            Map<String, Object> utilisateur = (Map<String, Object>) authResult.get("utilisateur");
            Long expiresIn = (Long) authResult.get("expiresIn");

            // Réponse de succès
            AuthResponse response = AuthResponse.success(
                    "Connexion réussie",
                    token,
                    utilisateur,
                    expiresIn
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            AuthResponse response = AuthResponse.error("Email ou mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (AuthenticationException e) {
            AuthResponse response = AuthResponse.error("Erreur d'authentification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            AuthResponse response = AuthResponse.error("Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = jwtUtil.extractTokenFromHeader(authorizationHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Token manquant"));
            }

            Optional<Utilisateur> utilisateurOpt = authService.validateTokenAndGetUser(token);
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                Map<String, Object> userData = authService.mapUtilisateurToResponse(utilisateur);
                return ResponseEntity.ok(userData);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Token invalide"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Erreur interne du serveur"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = jwtUtil.extractTokenFromHeader(authorizationHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Token manquant"));
            }

            Map<String, Object> refreshResult = authService.refreshToken(token);

            String newToken = (String) refreshResult.get("token");
            Map<String, Object> utilisateur = (Map<String, Object>) refreshResult.get("utilisateur");
            Long expiresIn = (Long) refreshResult.get("expiresIn");

            AuthResponse response = AuthResponse.success(
                    "Token rafraîchi avec succès",
                    newToken,
                    utilisateur,
                    expiresIn
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Impossible de rafraîchir le token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = jwtUtil.extractTokenFromHeader(authorizationHeader);
            if (token != null) {
                authService.logout(token);
            }

            AuthResponse response = AuthResponse.success("Déconnexion réussie", null, null, null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            AuthResponse response = AuthResponse.error("Erreur lors de la déconnexion");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "message", "API d'authentification opérationnelle",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
