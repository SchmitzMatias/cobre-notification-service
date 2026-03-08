package com.cobre.notification_service.delivery.application.ports.out;

import com.cobre.notification_service.delivery.domain.models.WebhookSubscription;

import java.util.Optional;

public interface SubscriptionRepositoryPort {

    Optional<WebhookSubscription> findByClientId(String clientId);

    void register(WebhookSubscription subscription);
}
