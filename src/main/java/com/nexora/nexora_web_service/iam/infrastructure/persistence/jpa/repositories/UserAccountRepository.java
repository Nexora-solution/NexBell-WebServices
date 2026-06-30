package com.nexora.nexora_web_service.iam.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.iam.domain.model.entities.UserAccount;
import com.nexora.nexora_web_service.iam.domain.model.valueobjects.EmailAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(EmailAddress email);
    boolean existsByEmail(EmailAddress email);
}
