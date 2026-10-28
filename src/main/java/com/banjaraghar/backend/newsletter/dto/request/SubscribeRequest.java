package com.banjaraghar.backend.newsletter.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class SubscribeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;
}
