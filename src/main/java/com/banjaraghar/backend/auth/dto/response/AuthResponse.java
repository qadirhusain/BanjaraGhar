package com.banjaraghar.backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;
    private UUID userId;
    private String email;
    private String fullName;
    private String role;
}
