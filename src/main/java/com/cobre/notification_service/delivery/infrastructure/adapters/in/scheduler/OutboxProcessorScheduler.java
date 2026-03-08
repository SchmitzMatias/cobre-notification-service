package com.cobre.notification_service.delivery.infrastructure.adapters.in.scheduler;

import com.cobre.notification_service.delivery.application.ports.in.ProcessPendingNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessorScheduler {

    private final ProcessPendingNotificationsUseCase deliveryUseCase;

    /**
     * Fires every 30 seconds after the previous execution completes.
     * Using fixedDelay (not fixedRate) to avoid overlapping runs if delivery is
     * slow.
     * Initial delay of 15 seconds to allow the application to start up and
     * initialize the in-memory store, preventing race condition
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void processOutbox() {
        log.info("OutboxProcessorScheduler: triggering outbox sweep.");
        deliveryUseCase.processAllPendingNotifications();
    }
}
