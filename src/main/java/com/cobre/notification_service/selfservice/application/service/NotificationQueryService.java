package com.cobre.notification_service.selfservice.application.service;

import com.cobre.notification_service.selfservice.application.ports.in.QueryNotificationsUseCase;
import com.cobre.notification_service.selfservice.application.ports.in.ReplayNotificationUseCase;
import com.cobre.notification_service.selfservice.application.ports.out.NotificationQueryPort;
import com.cobre.notification_service.selfservice.domain.models.NotificationView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueryService implements QueryNotificationsUseCase, ReplayNotificationUseCase {

    private final NotificationQueryPort port;

    @Override
    public List<NotificationView> findAll(String clientId, String status, LocalDate deliveryDate) {
        return port.findAll(clientId, status, deliveryDate);
    }

    @Override
    public NotificationView findById(String id) {
        return port.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification with id " + id + " not found"));
    }

    @Override
    @Transactional
    public void replay(String id) {
        NotificationView view = findById(id);

        // Only allow replaying failed states (or perhaps COMPLETED for testing,
        // but typically it's for failed webhooks)
        log.info("Replaying event_id={}. Previous status={}", id, view.getStatus());

        view.setStatus("PENDING");
        view.setRetryCount(0);
        view.setNextRetryAt(null);

        port.save(view);
    }
}
