package com.cobre.notification_service.delivery.application.ports.out;

public interface WebhookPort {

    /**
     * Sends a webhook notification to the given URL.
     *
     * @throws WebhookDeliveryException if the remote endpoint returns a non-2xx
     *                                  status
     *                                  or the request times out / fails at
     *                                  transport level.
     */
    void send(String webhookUrl, String eventId, String eventType, String content);
}
