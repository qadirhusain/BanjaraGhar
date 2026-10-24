package com.banjaraghar.backend.auth.repo;

import com.banjaraghar.backend.auth.entity.EmailOtp;
import com.banjaraghar.backend.auth.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {

    Optional<EmailOtp> findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    long countByEmailIgnoreCaseAndPurposeAndCreatedAtAfter(String email, OtpPurpose purpose, Instant since);

    void deleteByEmailIgnoreCase(String email);

    @Modifying
    @Query("delete from EmailOtp o where o.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
