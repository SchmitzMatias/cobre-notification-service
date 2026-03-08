package com.cobre.notification_service.delivery.domain.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Notification {
    private String eventId;
    private String eventType;
    private String content;
    private LocalDateTime deliveryDate;
    private String clientId;
    private NotificationStatus status;
    private int retryCount;
    private LocalDateTime nextRetryAt;
}
