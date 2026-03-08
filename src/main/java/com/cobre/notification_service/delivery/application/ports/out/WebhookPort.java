package com.cobre.notification_service.delivery.application.ports.out;

import java.time.LocalDateTime;

public interface WebhookPort {

    /**
     * Sends a webhook notification to the given URL.
     * Returns the delivery date to update the notification with, or a fallback.
     *
     * @throws WebhookDeliveryException if the remote endpoint returns a non-2xx
     *                                  status
     *                                  or the request times out / fails at
     *                                  transport level.
     */
    LocalDateTime send(String webhookUrl, String eventId, String eventType, String content);
}
