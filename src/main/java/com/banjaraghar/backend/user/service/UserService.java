package com.banjaraghar.backend.user.service;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.UnauthorizedException;
import com.banjaraghar.backend.user.dto.request.UpdateProfileRequest;
import com.banjaraghar.backend.user.dto.response.UserProfileResponse;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppProperties appProperties;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return toResponse(getActiveUser(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getActiveUser(userId);
        user.setFullName(request.getFullName().trim());

        String phone = request.getPhone() == null ? "" : request.getPhone().trim();
        user.setPhone(phone.isEmpty() ? null : phone);

        return toResponse(userRepository.save(user));
    }

    public User getActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new UnauthorizedException("Account not found"));
    }

    public boolean isAdminEmail(String email) {
        return appProperties.getAdminEmails().stream()
                .anyMatch(adminEmail -> adminEmail.trim().equalsIgnoreCase(email));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.getPasswordHash() != null,
                user.getCreatedAt()
        );
    }
}
