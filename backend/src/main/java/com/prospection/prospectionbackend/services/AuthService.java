package com.prospection.prospectionbackend.services;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import com.prospection.prospectionbackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service d'authentification - Version propre
 * Responsabilités:
 * - Authentification des utilisateurs
 * - Génération des tokens JWT
 * - Gestion des sessions
 */
@Service
@Transactional
public class AuthService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Méthode requise par UserDetailsService
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmailAndActifTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
    }

    /**
     * Authentification et génération du token
     */
    public Map<String, Object> login(String email, String motDePasse) throws AuthenticationException {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);
        System.out.println("Backend URL appelée: /api/auth/login");

        // Recherche de l'utilisateur actif
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmailAndActifTrue(email);

        if (utilisateurOpt.isEmpty()) {
            System.out.println(" Utilisateur non trouvé pour: " + email);
            throw new org.springframework.security.authentication.BadCredentialsException("Email ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        System.out.println("Utilisateur trouvé: " + utilisateur.getEmail());
        System.out.println("Hash stocké: " + utilisateur.getMotDePasse());
        System.out.println("Longueur hash: " + utilisateur.getMotDePasse().length());

        // Vérification du mot de passe
        boolean matches = passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse());
        System.out.println("Comparaison mot de passe: " + matches);

        if (!matches) {
            System.out.println("Mot de passe incorrect");
            throw new org.springframework.security.authentication.BadCredentialsException("Email ou mot de passe incorrect");
        }

        System.out.println("Authentification réussie");

        // Mise à jour de la dernière connexion
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        // Génération du token JWT
        String token = jwtUtil.generateToken(utilisateur);
        System.out.println("Token généré: " + token.substring(0, 20) + "...");

        // Préparation de la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("utilisateur", mapUtilisateurToResponse(utilisateur));
        response.put("expiresIn", System.currentTimeMillis() + 86400000); // 24h

        return response;
    }

    /**
     * Validation du token et récupération de l'utilisateur
     */
    public Optional<Utilisateur> validateTokenAndGetUser(String token) {
        try {
            String email = jwtUtil.getUsernameFromToken(token);
            if (email != null) {
                return utilisateurRepository.findByEmailAndActifTrue(email);
            }
        } catch (Exception e) {
            System.out.println("Token invalide: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Rafraîchissement du token
     */
    public Map<String, Object> refreshToken(String oldToken) {
        System.out.println("=== REFRESH TOKEN ===");

        Optional<Utilisateur> utilisateurOpt = validateTokenAndGetUser(oldToken);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            System.out.println("✅ Token valide pour: " + utilisateur.getEmail());

            // Générer un nouveau token
            String newToken = jwtUtil.generateToken(utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("utilisateur", mapUtilisateurToResponse(utilisateur));
            response.put("expiresIn", System.currentTimeMillis() + 86400000); // 24h

            System.out.println("Nouveau token généré");
            return response;
        }

        System.out.println("Token invalide pour refresh");
        throw new RuntimeException("Token invalide ou expiré");
    }

    /**
     * Déconnexion
     */
    public void logout(String token) {
        // Pour l'instant, pas de blacklist des tokens
        // Le token expirera naturellement après 24h
        System.out.println(" Déconnexion effectuée");
    }

    /**
     * Mapper l'utilisateur pour la réponse (sans mot de passe)
     */
    public Map<String, Object> mapUtilisateurToResponse(Utilisateur utilisateur) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", utilisateur.getId());
        userMap.put("nom", utilisateur.getNom());
        userMap.put("prenom", utilisateur.getPrenom());
        userMap.put("email", utilisateur.getEmail());
        userMap.put("role", utilisateur.getRole().name());
        userMap.put("roleDisplayName", utilisateur.getRole().getDisplayName());
        userMap.put("telephone", utilisateur.getTelephone());
        userMap.put("derniereConnexion", utilisateur.getDerniereConnexion());

        // Informations hiérarchiques
        if (utilisateur.getRegion() != null) {
            Map<String, Object> regionMap = new HashMap<>();
            regionMap.put("id", utilisateur.getRegion().getId());
            regionMap.put("nom", utilisateur.getRegion().getNom());
            regionMap.put("code", utilisateur.getRegion().getCode());
            userMap.put("region", regionMap);
        }

        if (utilisateur.getSupervision() != null) {
            Map<String, Object> supervisionMap = new HashMap<>();
            supervisionMap.put("id", utilisateur.getSupervision().getId());
            supervisionMap.put("nom", utilisateur.getSupervision().getNom());
            supervisionMap.put("code", utilisateur.getSupervision().getCode());
            userMap.put("supervision", supervisionMap);
        }

        if (utilisateur.getBranche() != null) {
            Map<String, Object> brancheMap = new HashMap<>();
            brancheMap.put("id", utilisateur.getBranche().getId());
            brancheMap.put("nom", utilisateur.getBranche().getNom());
            brancheMap.put("code", utilisateur.getBranche().getCode());
            userMap.put("branche", brancheMap);
        }

        return userMap;
    }
}