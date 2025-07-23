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
    private String token;
    private Map<String, Object> utilisateur;
    private Long expiresIn;
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public static AuthResponse success(String message, String token, Map<String, Object> utilisateur, Long expiresIn) {
        return new AuthResponse(true, message, token, utilisateur, expiresIn);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }
}
