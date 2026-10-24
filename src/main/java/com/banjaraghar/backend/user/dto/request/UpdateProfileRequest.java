package com.banjaraghar.backend.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    // Optional. Empty or missing clears the phone number.
    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Enter a valid phone number")
    private String phone;
}
