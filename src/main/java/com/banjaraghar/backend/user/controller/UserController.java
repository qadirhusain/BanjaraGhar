package com.banjaraghar.backend.user.controller;

import com.banjaraghar.backend.auth.dto.request.ChangePasswordRequest;
import com.banjaraghar.backend.auth.dto.response.AuthResponse;
import com.banjaraghar.backend.auth.service.AuthService;
import com.banjaraghar.backend.user.dto.request.DeleteAccountRequest;
import com.banjaraghar.backend.user.dto.request.UpdateProfileRequest;
import com.banjaraghar.backend.user.dto.response.UserProfileResponse;
import com.banjaraghar.backend.user.service.AccountDeletionService;
import com.banjaraghar.backend.user.service.UserService;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AccountDeletionService accountDeletionService;
    private final AuthService authService;

    @GetMapping("/me")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UUID userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(@AuthenticationPrincipal UUID userId,
                                             @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal UUID userId,
                              @RequestBody(required = false) DeleteAccountRequest request) {
        accountDeletionService.deleteAccount(userId, request);
    }

    @PutMapping("/me/password")
    public AuthResponse changePassword(@AuthenticationPrincipal UUID userId,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(userId, request);
    }
}
