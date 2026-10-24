package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.auth.dto.response.AuthResponse;
import com.banjaraghar.backend.security.jwt.JwtService;
import com.banjaraghar.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse issueTokens(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.issue(user),
                jwtService.getAccessTokenSeconds(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name());
    }
}