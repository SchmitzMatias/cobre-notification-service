package com.cobre.notification_service.delivery.application.ports.out;

import com.cobre.notification_service.delivery.domain.models.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepositoryPort {

    void save(Notification notification);

    void saveAll(List<Notification> notifications);

    /**
     * Returns all PENDING notifications that are ready to be processed:
     * those with no scheduled retry yet, or whose retry time has already passed.
     */
    List<Notification> findPendingDue(LocalDateTime now);
}
