package com.prospection.prospectionbackend.services;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import com.prospection.prospectionbackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
 * Service d'authentification et gestion des utilisateurs
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

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Méthode requise par UserDetailsService pour Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmailAndActifTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }

    /**
     * Authentification et génération du token JWT
     */
    public Map<String, Object> login(String email, String motDePasse) throws AuthenticationException {
        // Authentification avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, motDePasse)
        );

        // Récupération de l'utilisateur authentifié
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();

        // Mise à jour de la dernière connexion (approche simple)
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        // Génération du token JWT
        String token = jwtUtil.generateToken(utilisateur);

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
            // Token invalide
        }
        return Optional.empty();
    }

    /**
     * Rafraîchissement du token
     */
    public Map<String, Object> refreshToken(String oldToken) {
        Optional<Utilisateur> utilisateurOpt = validateTokenAndGetUser(oldToken);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            String newToken = jwtUtil.generateToken(utilisateur);

            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("utilisateur", mapUtilisateurToResponse(utilisateur));
            response.put("expiresIn", System.currentTimeMillis() + 86400000);

            return response;
        }

        throw new RuntimeException("Token invalide ou expiré");
    }

    /**
     * Déconnexion (côté serveur - optionnel)
     */
    public void logout(String token) {
        // Pour l'instant, pas de blacklist des tokens
        // Le token expirera naturellement après 24h
        // On pourrait implémenter une blacklist Redis si nécessaire
    }

    /**
     * Obtenir l'utilisateur actuel à partir du token
     */
    public Optional<Utilisateur> getCurrentUser(String token) {
        return validateTokenAndGetUser(token);
    }

    /**
     * Mapper l'utilisateur pour la réponse (sans mot de passe) - PUBLIC
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