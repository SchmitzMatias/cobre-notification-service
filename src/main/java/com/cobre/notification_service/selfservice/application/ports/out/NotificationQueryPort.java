package com.cobre.notification_service.selfservice.application.ports.out;

import com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence.NotificationQueryEntity;

import java.util.List;
import java.util.Optional;

public interface NotificationQueryPort {

    List<NotificationQueryEntity> findAll();

    Optional<NotificationQueryEntity> findById(String eventId);

    List<NotificationQueryEntity> findByClientId(String clientId);
}
