package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.auth.entity.RefreshToken;
import com.banjaraghar.backend.auth.repo.RefreshTokenRepository;
import com.banjaraghar.backend.common.util.HashUtil;
import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.UnauthorizedException;
import com.banjaraghar.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String INVALID_TOKEN_MESSAGE = "Session expired. Please sign in again.";

    private final SecureRandom random = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setHashedToken(HashUtil.sha256(rawToken));
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(appProperties.getJwt().getRefreshTokenDays())));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public User consume(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByHashedToken(HashUtil.sha256(rawToken))
                .filter(token -> !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new UnauthorizedException(INVALID_TOKEN_MESSAGE));

        User user = refreshToken.getUser();
        if (user.getDeletedAt() != null) {
            throw new UnauthorizedException(INVALID_TOKEN_MESSAGE);
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return user;
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByHashedToken(HashUtil.sha256(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
