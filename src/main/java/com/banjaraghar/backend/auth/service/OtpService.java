package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.auth.entity.EmailOtp;
import com.banjaraghar.backend.auth.enums.OtpPurpose;
import com.banjaraghar.backend.auth.repo.EmailOtpRepository;
import com.banjaraghar.backend.email.EmailService;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final long VALIDITY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_SENDS_PER_WINDOW = 3;
    private static final long RATE_LIMIT_WINDOW_MINUTES = 10;
    private static final String INVALID_CODE_MESSAGE = "Invalid or expired code";

    private final SecureRandom random = new SecureRandom();

    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendOtp(String email, OtpPurpose purpose) {
        Instant now = Instant.now();
        long recentSends = emailOtpRepository.countByEmailIgnoreCaseAndPurposeAndCreatedAtAfter(
                email, purpose, now.minus(Duration.ofMinutes(RATE_LIMIT_WINDOW_MINUTES)));
        if (recentSends >= MAX_SENDS_PER_WINDOW) {
            throw new TooManyRequestsException("Too many codes requested. Please try again in a few minutes.");
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setPurpose(purpose);
        emailOtp.setHashedOtp(passwordEncoder.encode(otp));
        emailOtp.setExpiresAt(now.plus(Duration.ofMinutes(VALIDITY_MINUTES)));
        emailOtpRepository.save(emailOtp);

        // Send only after the database transaction commits, so no code is emailed for a signup that rolled back
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendOtpEmail(email, otp, purpose, VALIDITY_MINUTES);
            }
        });
    }

    // noRollbackFor: a wrong guess must still save the incremented attempt count
    @Transactional(noRollbackFor = BadRequestException.class)
    public void verifyOtp(String email, OtpPurpose purpose, String otp) {

        EmailOtp emailOtp = emailOtpRepository
                .findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE_MESSAGE));

        if (emailOtp.isUsed() || emailOtp.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException(INVALID_CODE_MESSAGE);
        }
        if (emailOtp.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Too many wrong attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(otp, emailOtp.getHashedOtp())) {
            emailOtp.setAttempts(emailOtp.getAttempts() + 1);
            emailOtpRepository.save(emailOtp);
            throw new BadRequestException(INVALID_CODE_MESSAGE);
        }

        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);
    }
}
