package com.nexora.nexora_web_service.intercom.infrastructure.persistence.jpa.repositories;

import com.nexora.nexora_web_service.intercom.domain.model.entities.NotificationDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDispatchRepository extends JpaRepository<NotificationDispatch, Long> {
    @Query("SELECT vr.notification FROM VisitRequest vr WHERE vr.apartmentId = :apartmentId AND vr.notification IS NOT NULL")
    List<NotificationDispatch> findByApartmentId(@Param("apartmentId") Long apartmentId);
}
