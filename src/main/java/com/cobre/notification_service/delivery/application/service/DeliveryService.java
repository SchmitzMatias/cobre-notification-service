package com.cobre.notification_service.delivery.application.service;

import com.cobre.notification_service.delivery.application.ports.in.ProcessPendingNotificationsUseCase;
import com.cobre.notification_service.delivery.application.ports.out.NotificationRepositoryPort;
import com.cobre.notification_service.delivery.application.ports.out.SubscriptionRepositoryPort;
import com.cobre.notification_service.delivery.application.ports.out.WebhookPort;
import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import com.cobre.notification_service.delivery.domain.models.WebhookSubscription;
import com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence.NotificationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService implements ProcessPendingNotificationsUseCase {

    static final int MAX_RETRIES = 5;

    private final NotificationRepositoryPort notificationRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final WebhookPort webhookPort;

    @Override
    public void processAllPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationEntity> pending = notificationRepository.findPendingDue(now);

        log.info("DeliveryService: Processing {} pending notification(s).", pending.size());

        for (NotificationEntity notification : pending) {
            processSingle(notification, now);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void processSingle(NotificationEntity notification, LocalDateTime now) {
        String clientId = notification.getClientId();
        Optional<WebhookSubscription> subscriptionOpt = subscriptionRepository.findByClientId(clientId);

        if (subscriptionOpt.isEmpty()) {
            log.warn("DeliveryService: No active subscription for clientId={} — marking event={} as FAILED.",
                    clientId, notification.getEventId());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            return;
        }

        String webhookUrl = subscriptionOpt.get().webhookUrl();

        try {
            webhookPort.send(webhookUrl, notification.getEventId(),
                    notification.getEventType(), notification.getContent());

            log.info("DeliveryService: event={} delivered successfully to {}.",
                    notification.getEventId(), webhookUrl);
            notification.setStatus(NotificationStatus.COMPLETED);

        } catch (Exception ex) {
            handleDeliveryFailure(notification, now, webhookUrl, ex);
        }

        notificationRepository.save(notification);
    }

    private void handleDeliveryFailure(NotificationEntity notification, LocalDateTime now,
            String webhookUrl, Exception cause) {
        int newRetryCount = notification.getRetryCount() + 1;
        notification.setRetryCount(newRetryCount);

        if (newRetryCount >= MAX_RETRIES) {
            log.error("DeliveryService: event={} reached max retries ({}) — marking FAILED. url={}",
                    notification.getEventId(), MAX_RETRIES, webhookUrl, cause);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setNextRetryAt(null);
        } else {
            // Exponential backoff: 2^retryCount minutes from now
            long backoffMinutes = (long) Math.pow(2, newRetryCount);
            LocalDateTime nextRetry = now.plusMinutes(backoffMinutes);

            log.warn("DeliveryService: event={} delivery failed (attempt {}/{}). Next retry at {} (+{} min). url={}",
                    notification.getEventId(), newRetryCount, MAX_RETRIES,
                    nextRetry, backoffMinutes, webhookUrl, cause);

            notification.setNextRetryAt(nextRetry);
            // status stays PENDING
        }
    }
}
