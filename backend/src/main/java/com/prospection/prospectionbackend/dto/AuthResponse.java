package com.prospection.prospectionbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Map<String, Object> utilisateur;
    private Long expiresIn;
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public static AuthResponse success(String message, String accessToken, String refreshToken, Map<String, Object> utilisateur, Long expiresIn) {
        AuthResponse response = new AuthResponse();
        response.success = true;
        response.message = message;
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.utilisateur = utilisateur;
        response.expiresIn = expiresIn;
        return response;
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }
}
