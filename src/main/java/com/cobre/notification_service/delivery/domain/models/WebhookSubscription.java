package com.cobre.notification_service.delivery.domain.models;

/**
 * Domain value object representing a client's webhook subscription.
 *
 * <p>
 * Currently backed by an in-memory registry
 * ({@code InMemorySubscriptionAdapter}).
 * Designed for a clean future migration to a JPA entity + DB table:
 * just swap the adapter behind {@code SubscriptionRepositoryPort} — no domain
 * or
 * application-layer changes needed.
 */
public record WebhookSubscription(String clientId, String webhookUrl) {
}
