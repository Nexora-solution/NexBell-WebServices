package com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.intercom.domain.model.entities.IntercomQueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercomQueueItemRepository extends JpaRepository<IntercomQueueItem, Long> {
    List<IntercomQueueItem> findByStatus(String status);
}
