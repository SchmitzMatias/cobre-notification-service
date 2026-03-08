package com.cobre.notification_service.delivery.application.ports.out;

import com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence.NotificationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {

    NotificationEntity save(NotificationEntity entity);

    List<NotificationEntity> saveAll(List<NotificationEntity> entities);

    Optional<NotificationEntity> findById(String eventId);

    List<NotificationEntity> findAll();

    /**
     * Returns all PENDING notifications that are ready to be processed:
     * those with no scheduled retry yet, or whose retry time has already passed.
     */
    List<NotificationEntity> findPendingDue(LocalDateTime now);
}
