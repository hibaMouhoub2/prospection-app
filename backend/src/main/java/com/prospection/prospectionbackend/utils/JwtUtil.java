package com.prospection.prospectionbackend.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.prospection.prospectionbackend.entities.Utilisateur;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private String jwtExpiration;

    private static final String ISSUER = "prospection-app";

    public String generateToken(Utilisateur utilisateur) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(utilisateur.getEmail())
                .withClaim("userId", utilisateur.getId())
                .withClaim("nom", utilisateur.getNom())
                .withClaim("prenom", utilisateur.getPrenom())
                .withClaim("role", utilisateur.getRole().name())
                .withClaim("regionId", utilisateur.getRegion() != null ? utilisateur.getRegion().getId(): null)
                .withClaim("supervisionId", utilisateur.getSupervision() != null ? utilisateur.getSupervision().getId() : null)
                .withClaim("brancheId", utilisateur.getBranche() != null ? utilisateur.getBranche().getId() : null)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(algorithm);

    }

    public DecodedJWT validateToken(String token)throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    public String getUsernameFromToken(String token)throws JWTVerificationException {
        DecodedJWT jwt = validateToken(token);
        return jwt.getSubject();
    }

    public String getUserIdFromToken(String token)throws JWTVerificationException {
        DecodedJWT jwt = validateToken(token);
        return jwt.getClaim("userId").asString();
    }

    public String getRoleFromToken(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getClaim("role").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
