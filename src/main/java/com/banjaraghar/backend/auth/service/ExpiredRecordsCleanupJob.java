package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.auth.repo.EmailOtpRepository;
import com.banjaraghar.backend.auth.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Component
@Slf4j
public class ExpiredRecordsCleanupJob {

    private final EmailOtpRepository emailOtpRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void deleteExpiredRecords() {
        Instant now = Instant.now();

        int otps = emailOtpRepository.deleteExpiredBefore(now.minus(Duration.ofDays(1)));
        int tokens = refreshTokenRepository.deleteExpired(now);
        log.info("Cleanup removed {} expired OTPs and {} expired refresh tokens", otps, tokens);
    }
}
