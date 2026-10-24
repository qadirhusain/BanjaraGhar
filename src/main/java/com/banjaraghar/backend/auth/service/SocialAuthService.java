package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.auth.client.FacebookTokenVerifier;
import com.banjaraghar.backend.auth.client.GoogleTokenVerifier;
import com.banjaraghar.backend.auth.client.SocialProfile;
import com.banjaraghar.backend.auth.dto.response.AuthResponse;
import com.banjaraghar.backend.auth.entity.AuthIdentity;
import com.banjaraghar.backend.auth.enums.AuthProvider;
import com.banjaraghar.backend.auth.repo.AuthIdentityRepository;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.enums.Role;
import com.banjaraghar.backend.user.repo.UserRepository;
import com.banjaraghar.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private static final int MAX_NAME_LENGTH = 150;

    private final GoogleTokenVerifier googleTokenVerifier;
    private final FacebookTokenVerifier facebookTokenVerifier;
    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final TokenService tokenService;
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    public AuthResponse loginWithGoogle(String idToken) {
        SocialProfile profile = googleTokenVerifier.verify(idToken);   // network call, outside the transaction
        return signInWithRetry(AuthProvider.GOOGLE, profile);
    }

    public AuthResponse loginWithFacebook(String accessToken) {
        SocialProfile profile = facebookTokenVerifier.verify(accessToken);
        return signInWithRetry(AuthProvider.FACEBOOK, profile);
    }

    private AuthResponse signInWithRetry(AuthProvider provider, SocialProfile profile) {
        try {
            return transactionTemplate.execute(_ -> signIn(provider, profile));
        } catch (DataIntegrityViolationException e) {
            // Two first-time sign-ins for the same person arrived together and the other one won.
            // Trying once more finds the account it created.
            return transactionTemplate.execute(_ -> signIn(provider, profile));
        }
    }

    private AuthResponse signIn(AuthProvider provider, SocialProfile profile) {
        User user = findUserByIdentity(provider, profile.getProviderUserId())
                .orElseGet(() -> linkOrCreateUser(provider, profile));

        if (userService.isAdminEmail(user.getEmail()) && user.getRole() != Role.ADMIN) {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        }
        return tokenService.issueTokens(user);
    }

    private Optional<User> findUserByIdentity(AuthProvider provider, String providerUserId) {
        Optional<AuthIdentity> identity = authIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (identity.isEmpty()) {
            return Optional.empty();
        }
        User user = identity.get().getUser();
        if (user.getDeletedAt() == null) {
            return Optional.of(user);
        }
        // Left over from a deleted account: remove it so this login can start fresh
        authIdentityRepository.delete(identity.get());
        authIdentityRepository.flush();
        return Optional.empty();
    }

    private User linkOrCreateUser(AuthProvider provider, SocialProfile profile) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(profile.getEmail()).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(profile.getEmail());
            user.setFullName(displayName(profile));
            user.setEmailVerified(true);
        } else if (!user.isEmailVerified()) {
            // Someone may have registered this email with a password but never proved they own it.
            // The provider has now proved ownership, so that unverified password is discarded.
            user.setPasswordHash(null);
            user.setEmailVerified(true);
        }
        user = userRepository.save(user);

        AuthIdentity identity = new AuthIdentity();
        identity.setUser(user);
        identity.setProvider(provider);
        identity.setProviderUserId(profile.getProviderUserId());
        authIdentityRepository.save(identity);

        return user;
    }

    private String displayName(SocialProfile profile) {
        String name = profile.getFullName();
        if (name == null || name.isBlank()) {
            name = profile.getEmail().substring(0, profile.getEmail().indexOf('@'));
        }
        name = name.trim();
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }
}
