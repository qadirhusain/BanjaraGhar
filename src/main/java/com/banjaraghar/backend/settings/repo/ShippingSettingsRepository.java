package com.banjaraghar.backend.settings.repo;

import com.banjaraghar.backend.settings.entity.ShippingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShippingSettingsRepository extends JpaRepository<ShippingSettings, UUID> {

    // The table always holds exactly one row
    Optional<ShippingSettings> findFirstBy();
}