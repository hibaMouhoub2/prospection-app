package com.prospection.prospectionbackend.config;

import com.prospection.prospectionbackend.entities.Utilisateur;
import com.prospection.prospectionbackend.repositories.UtilisateurRepository;
import com.prospection.prospectionbackend.services.AuthService;
import com.prospection.prospectionbackend.services.TokenBlacklistService;
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


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UtilisateurRepository utilisateurRepository;


    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {



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
            if (tokenBlacklistService.isTokenBlacklisted(jwtToken)) {
                System.out.println("Token blacklisté - accès refusé");
                filterChain.doFilter(request, response);
                return;
            }
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


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {

                Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmailWithRelations(username);



                if (utilisateurOpt.isPresent()) {
                    Utilisateur utilisateur = utilisateurOpt.get();

                    // Validation du token
                    jwtUtil.validateToken(jwtToken);
                    System.out.println("Token validé pour: " + username);


                    try {
                        if (utilisateur.getRegion() != null) {
                            utilisateur.getRegion().getNom();
                        }
                        if (utilisateur.getSupervision() != null) {
                            utilisateur.getSupervision().getNom();
                        }
                        if (utilisateur.getBranche() != null) {
                            utilisateur.getBranche().getNom();
                        }
                    } catch (Exception e) {
                        System.out.println("Avertissement: Impossible de charger les relations de l'utilisateur: " + e.getMessage());

                    }


                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    utilisateur,
                                    null,
                                    utilisateur.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));


                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentification définie avec utilisateur: " + utilisateur.getEmail());
                    System.out.println("Principal type: " + authToken.getPrincipal().getClass().getName());

                } else {
                    System.out.println("Utilisateur non trouvé: " + username);
                }

            } catch (Exception e) {
                System.out.println("Erreur lors de l'authentification JWT: " + e.getMessage());
                e.printStackTrace();

            }
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
                "OPTIONS".equals(method);

        if (shouldNotFilter) {
            System.out.println("Exclusion du filtre JWT pour: " + path);
        }

        return shouldNotFilter;
    }
}