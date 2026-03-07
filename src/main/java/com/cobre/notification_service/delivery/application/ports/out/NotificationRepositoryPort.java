package com.cobre.notification_service.delivery.application.ports.out;

import com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence.NotificationEntity;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {

    NotificationEntity save(NotificationEntity entity);

    List<NotificationEntity> saveAll(List<NotificationEntity> entities);

    Optional<NotificationEntity> findById(String eventId);

    List<NotificationEntity> findAll();
}
