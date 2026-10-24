package com.banjaraghar.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacebookLoginRequest {

    @NotBlank(message = "Facebook token is required")
    private String accessToken;
}