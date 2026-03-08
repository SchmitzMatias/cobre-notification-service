package com.cobre.notification_service.bootstrap;

import com.cobre.notification_service.delivery.domain.models.Notification;
import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import com.cobre.notification_service.delivery.domain.models.WebhookSubscription;
import com.cobre.notification_service.delivery.application.ports.out.NotificationRepositoryPort;
import com.cobre.notification_service.delivery.application.ports.out.SubscriptionRepositoryPort;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final String CLIENT003_WEBHOOK = "https://webhook.site/226d7a72-da0e-4bae-a480-5332c6878845";
    // private static final String CLIENT003_WEBHOOK =
    // "https://httpbin.org/status/404";

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ObjectMapper mapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("DataLoader: Starting seed data load from notification_events.json");

        ClassPathResource resource = new ClassPathResource("notification_events.json");
        try (InputStream inputStream = resource.getInputStream()) {

            NotificationEventsWrapper wrapper = mapper.readValue(
                    inputStream,
                    NotificationEventsWrapper.class);

            List<NotificationEventRecord> records = wrapper.getEvents();

            List<Notification> entities = records.stream()
                    .map(this::mapToDomain)
                    .toList();

            notificationRepositoryPort.saveAll(entities);
            log.info("DataLoader: Successfully seeded {} notification events.", entities.size());

        } catch (Exception e) {
            log.error("DataLoader: Failed to load seed data.", e);
            throw e;
        }

        registerSubscriptions();
    }

    /**
     * Registers known webhook subscriptions in the in-memory store.
     * CLIENT002 and CLIENT003 are intentionally left unregistered so the
     * "no subscription → FAILED" delivery path is exercised on startup.
     *
     * TODO: load subscriptions from DB when the webhook_subscriptions table is
     * introduced.
     */
    private void registerSubscriptions() {
        subscriptionRepositoryPort.register(new WebhookSubscription("CLIENT003", CLIENT003_WEBHOOK));
        log.info("DataLoader: Registered webhook subscription for CLIENT003 -> {}", CLIENT003_WEBHOOK);
    }

    private Notification mapToDomain(NotificationEventRecord record) {
        NotificationStatus status;
        if ("completed".equalsIgnoreCase(record.getDeliveryStatus())) {
            status = NotificationStatus.COMPLETED;
        } else if ("failed".equalsIgnoreCase(record.getDeliveryStatus())) {
            status = NotificationStatus.FAILED;
        } else {
            status = NotificationStatus.PENDING;
        }

        return Notification.builder()
                .eventId(record.getEventId())
                .eventType(record.getEventType())
                .content(record.getContent())
                .deliveryDate(record.getDeliveryDate())
                .clientId(record.getClientId())
                .status(status)
                .retryCount(0)
                .nextRetryAt(null)
                .build();
    }

    /**
     * Wrapper for the JSON root object.
     */
    @Data
    static class NotificationEventsWrapper {

        @JsonProperty("events")
        private List<NotificationEventRecord> events;

    }

    /**
     * Internal DTO used only for deserializing the JSON seed file.
     */
    @Data
    static class NotificationEventRecord {

        @JsonProperty("event_id")
        private String eventId;

        @JsonProperty("event_type")
        private String eventType;

        @JsonProperty("content")
        private String content;

        @JsonProperty("delivery_date")
        private LocalDateTime deliveryDate;

        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("delivery_status")
        private String deliveryStatus;
    }
}