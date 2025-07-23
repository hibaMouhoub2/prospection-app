package com.prospection.prospectionbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT temporairement désactivé pour éviter la dépendance circulaire
 * TODO: Réactiver après avoir résolu le problème de dépendances
 */
//@Component  // COMMENTÉ TEMPORAIREMENT
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Pour l'instant, on laisse passer toutes les requêtes sans vérification JWT
        // TODO: Ajouter la logique JWT plus tard
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Ne pas filtrer pour l'instant
        return true;
    }
}