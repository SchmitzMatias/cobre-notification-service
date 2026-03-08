package com.cobre.notification_service.delivery.application.ports.in;

public interface ProcessPendingNotificationsUseCase {

    /**
     * Fetches all due-pending notifications and attempts delivery,
     * applying exponential backoff on failures.
     */
    void processAllPendingNotifications();
}
