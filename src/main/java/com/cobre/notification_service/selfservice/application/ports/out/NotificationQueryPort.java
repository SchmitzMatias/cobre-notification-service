package com.cobre.notification_service.selfservice.application.ports.out;

import com.cobre.notification_service.selfservice.domain.models.NotificationView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NotificationQueryPort {

    List<NotificationView> findAll(String clientId, String status, LocalDate deliveryDate);

    Optional<NotificationView> findById(String eventId);

    void save(NotificationView view);
}
