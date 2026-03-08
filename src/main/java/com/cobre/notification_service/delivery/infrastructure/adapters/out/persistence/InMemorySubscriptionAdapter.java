package com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.delivery.domain.models.WebhookSubscription;
import com.cobre.notification_service.delivery.application.ports.out.SubscriptionRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link SubscriptionRepositoryPort}.
 *
 * <p>
 * Thread-safe via {@link ConcurrentHashMap}.
 *
 * <p>
 * TODO: Replace with a JPA-backed adapter when a {@code webhook_subscriptions}
 * table
 * is introduced. The hexagonal port ensures zero changes to the domain or
 * application layer.
 */
@Component
public class InMemorySubscriptionAdapter implements SubscriptionRepositoryPort {

    private final Map<String, WebhookSubscription> store = new ConcurrentHashMap<>();

    @Override
    public Optional<WebhookSubscription> findByClientId(String clientId) {
        return Optional.ofNullable(store.get(clientId));
    }

    @Override
    public void register(WebhookSubscription subscription) {
        store.put(subscription.clientId(), subscription);
    }
}
