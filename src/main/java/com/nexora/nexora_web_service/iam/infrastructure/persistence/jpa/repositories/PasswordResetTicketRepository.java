package com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.iam.domain.model.entities.PasswordResetTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTicketRepository extends JpaRepository<PasswordResetTicket, Long> {
    Optional<PasswordResetTicket> findByToken(String token);
}
