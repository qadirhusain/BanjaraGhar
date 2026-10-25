package com.banjaraghar.backend.user.service;

import com.banjaraghar.backend.auth.repo.AuthIdentityRepository;
import com.banjaraghar.backend.auth.repo.EmailOtpRepository;
import com.banjaraghar.backend.auth.service.RefreshTokenService;
import com.banjaraghar.backend.cart.repo.CartItemRepository;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.user.dto.request.DeleteAccountRequest;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.repo.AddressRepository;
import com.banjaraghar.backend.user.repo.UserRepository;
import com.banjaraghar.backend.wishlist.repo.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final WishlistItemRepository wishlistItemRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public void deleteAccount(UUID userId, DeleteAccountRequest request) {
        User user = userService.getActiveUser(userId);

        if (user.getPasswordHash() != null) {
            String password = request == null ? null : request.getPassword();

            if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BadRequestException("Incorrect password");
            }
        }

        String originalEmail = user.getEmail();

        addressRepository.deleteByUserId(userId);
        wishlistItemRepository.deleteByUserId(userId);
        cartItemRepository.deleteByUserId(userId);
        authIdentityRepository.deleteByUserId(userId);
        emailOtpRepository.deleteByEmailIgnoreCase(originalEmail);
        refreshTokenService.revokeAllForUser(userId);

        user.setEmail("deleted-" + userId + "@deleted.invalid");
        user.setFullName("Deleted user");
        user.setPhone(null);
        user.setPasswordHash(null);
        user.setEmailVerified(false);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }
}
