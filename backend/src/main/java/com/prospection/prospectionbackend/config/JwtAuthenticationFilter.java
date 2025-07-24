package com.prospection.prospectionbackend.config;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import com.prospection.prospectionbackend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filtre JWT - Version sans dépendance circulaire
 * Utilise directement le repository au lieu du service AuthService
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Debug - voir quelles requêtes passent par le filtre
        String path = request.getRequestURI();
        System.out.println("JWT Filter - URI: " + path + " | Method: " + request.getMethod());

        // Si c'est un endpoint public, passer sans vérification
        if (shouldNotFilter(request)) {
            System.out.println("Endpoint public - pas de vérification JWT");
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + (authorizationHeader != null ? "Présent" : "Absent"));

        String username = null;
        String jwtToken = null;

        // Extraction du token JWT
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            System.out.println("Token extrait: " + jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");

            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
                System.out.println(" Username extrait du token: " + username);
            } catch (Exception e) {
                System.out.println("Token JWT invalide: " + e.getMessage());
            }
        } else {
            System.out.println("Pas de token Bearer dans le header");
        }

        // Validation et authentification
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Récupération directe de l'utilisateur (évite la dépendance circulaire)
                Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmailAndActifTrue(username);

                if (utilisateurOpt.isPresent()) {
                    Utilisateur utilisateur = utilisateurOpt.get();

                    // Validation du token
                    jwtUtil.validateToken(jwtToken);
                    System.out.println(" Token validé pour: " + username);

                    // Création de l'authentification
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    utilisateur, null, utilisateur.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Définition du contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println(" Authentification définie dans le contexte");

                } else {
                    System.out.println(" Utilisateur non trouvé: " + username);
                }

            } catch (Exception e) {
                System.out.println("Erreur lors de l'authentification JWT: " + e.getMessage());
            }
        } else if (username == null) {
            System.out.println(" Pas de username dans le token");
        } else {
            System.out.println("Authentification déjà présente dans le contexte");
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();


        boolean shouldNotFilter = path.equals("/api/auth/login") ||
                path.equals("/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/auth/register") ||
                path.equals("/api/auth/ping") ||
                path.equals("/auth/ping") ||
                path.startsWith("/structure/") ||
                path.startsWith("/h2-console") ||
                path.equals("/actuator/health") ||
                path.equals("/error") ||
                "OPTIONS".equals(method); // CORS preflight

        if (shouldNotFilter) {
            System.out.println("Exclusion du filtre JWT pour: " + path);
        }

        return shouldNotFilter;
    }
}