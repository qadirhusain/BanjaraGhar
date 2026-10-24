package com.banjaraghar.backend.auth.repo;

import com.banjaraghar.backend.auth.entity.AuthIdentity;
import com.banjaraghar.backend.auth.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, UUID> {

    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    void deleteByUserId(UUID userId);
}
