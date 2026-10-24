package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.auth.dto.request.*;
import com.banjaraghar.backend.auth.dto.response.AuthResponse;
import com.banjaraghar.backend.auth.enums.OtpPurpose;
import com.banjaraghar.backend.common.response.MessageResponse;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ForbiddenException;
import com.banjaraghar.backend.exception.UnauthorizedException;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.enums.Role;
import com.banjaraghar.backend.user.repo.UserRepository;
import com.banjaraghar.backend.user.service.UserService;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String email = normalize(request.getEmail());

        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).orElse(null);
        if (user != null && user.isEmailVerified()) {
            throw new ConflictException("An account with this email already exists");
        }

        if (user == null) {
            user = new User();
            user.setEmail(email);
        }

        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        otpService.sendOtp(email, OtpPurpose.SIGNUP);
        return new MessageResponse("Verification code sent to " + email);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public MessageResponse verifyEmail(VerifyOtpRequest request) {
        String email = normalize(request.getEmail());

        otpService.verifyOtp(email, OtpPurpose.SIGNUP, request.getOtp());

        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code"));

        user.setEmailVerified(true);
        if (userService.isAdminEmail(email)) {
            user.setRole(Role.ADMIN);
        }
        userRepository.save(user);

        return new MessageResponse("Email verified. You can now sign in.");
    }

    @Transactional
    public MessageResponse resendOtp(ResendOtpRequest request) {
        String email = normalize(request.getEmail());

        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(_ -> otpService.sendOtp(email, OtpPurpose.SIGNUP));

        return new MessageResponse("If this email is awaiting verification, a new code has been sent.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        String email = normalize(request.getEmail());
        String attemptKey = email + "|" + clientIp;

        loginAttemptService.checkAllowed(attemptKey);

        // Same message for "no such user", "wrong password" and "social-only account"
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .orElse(null);

        if (user == null) {
            loginAttemptService.recordFailure(attemptKey);
            throw new UnauthorizedException("Invalid email or password");
        }
        loginAttemptService.recordSuccess(attemptKey);

        if (!user.isEmailVerified()) {
            throw new ForbiddenException("Please verify your email before signing in");
        }
        return tokenService.issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = refreshTokenService.consume(request.getRefreshToken());
        return tokenService.issueTokens(user);
    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return new MessageResponse("Signed out");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalize(request.getEmail());

        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(User::isEmailVerified)
                .ifPresent(_ -> otpService.sendOtp(email, OtpPurpose.PASSWORD_RESET));

        return new MessageResponse("If an account exists for this email, a reset code has been sent.");
    }

    @Transactional
    public AuthResponse changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userService.getActiveUser(userId);

        if (user.getPasswordHash() == null) {
            throw new BadRequestException("Your account has no password yet. Use \"Forgot password\" to set one.");
        }

        String attemptKey = "change-password|" + userId;
        loginAttemptService.checkAllowed(attemptKey);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            loginAttemptService.recordFailure(attemptKey);
            // 400, not 401: a 401 would make the app sign the user out
            throw new BadRequestException("Current password is incorrect");
        }
        loginAttemptService.recordSuccess(attemptKey);

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("New password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Sign out every device, then give this one a fresh session
        refreshTokenService.revokeAllForUser(userId);
        return tokenService.issueTokens(user);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = normalize(request.getEmail());

        otpService.verifyOtp(email, OtpPurpose.PASSWORD_RESET, request.getOtp());

        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Anyone holding an old session, including a thief, is signed out
        refreshTokenService.revokeAllForUser(user.getId());

        return new MessageResponse("Password updated. Please sign in with your new password.");
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

}
