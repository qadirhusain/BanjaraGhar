package com.banjaraghar.backend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private boolean emailVerified;
    private boolean hasPassword;   // false for Google/Facebook-only accounts, so the app can offer "set a password"
    private Instant createdAt;
}
