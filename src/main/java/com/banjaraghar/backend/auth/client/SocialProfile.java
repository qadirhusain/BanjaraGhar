package com.banjaraghar.backend.auth.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialProfile {

    private String providerUserId;
    private String email;
    private String fullName;
}
