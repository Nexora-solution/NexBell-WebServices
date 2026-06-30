package com.nexora.nexora_web_service.security.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.security.domain.model.entities.IoTDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IoTDeviceRepository extends JpaRepository<IoTDevice, Long> {
    Optional<IoTDevice> findByDeviceCode(String deviceCode);
}
