package com.prospection.prospectionbackend.services;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = new HashSet<>();

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public void blacklistTokens(String accessToken, String refreshToken) {
        if (accessToken != null) {
            blacklistedTokens.add(accessToken);
        }
        if (refreshToken != null) {
            blacklistedTokens.add(refreshToken);
        }
    }
}