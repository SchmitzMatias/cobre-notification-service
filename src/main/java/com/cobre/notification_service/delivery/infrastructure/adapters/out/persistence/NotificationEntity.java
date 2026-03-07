package com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_events")
public class NotificationEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "client_id")
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "webhook_url")
    private String webhookUrl;
}
