package com.cobre.notification_service.selfservice.application.ports.in;

import com.cobre.notification_service.selfservice.domain.models.NotificationView;

import java.time.LocalDate;
import java.util.List;

public interface QueryNotificationsUseCase {

    /**
     * Finds notifications matching the given filters. All parameters are optional.
     */
    List<NotificationView> findAll(String clientId, String status, LocalDate deliveryDate);

    /**
     * Finds a single notification by its ID.
     */
    NotificationView findById(String id);
}
