package com.cobre.notification_service.bootstrap;

import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence.NotificationEntity;
import com.cobre.notification_service.delivery.application.ports.out.NotificationRepositoryPort;
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

    private final NotificationRepositoryPort notificationRepositoryPort;
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

            List<NotificationEntity> entities = records.stream()
                    .map(this::mapToEntity)
                    .toList();

            notificationRepositoryPort.saveAll(entities);
            log.info("DataLoader: Successfully seeded {} notification events.", entities.size());

        } catch (Exception e) {
            log.error("DataLoader: Failed to load seed data.", e);
            throw e;
        }
    }

    private NotificationEntity mapToEntity(NotificationEventRecord record) {
        // Business rule: "completed" -> COMPLETED, "failed" -> PENDING (retry pickup)
        NotificationStatus status = "completed".equalsIgnoreCase(record.getDeliveryStatus())
                ? NotificationStatus.COMPLETED
                : NotificationStatus.PENDING;

        return NotificationEntity.builder()
                .eventId(record.getEventId())
                .eventType(record.getEventType())
                .content(record.getContent())
                .deliveryDate(record.getDeliveryDate())
                .clientId(record.getClientId())
                .status(status)
                .retryCount(0)
                .nextRetryAt(null)
                .webhookUrl(null)
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