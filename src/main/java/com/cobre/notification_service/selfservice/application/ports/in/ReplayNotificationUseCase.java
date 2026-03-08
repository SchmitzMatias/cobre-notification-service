package com.cobre.notification_service.selfservice.application.ports.in;

public interface ReplayNotificationUseCase {

    /**
     * Resets a notification's state so the delivery engine picks it up again.
     */
    void replay(String id);
}
