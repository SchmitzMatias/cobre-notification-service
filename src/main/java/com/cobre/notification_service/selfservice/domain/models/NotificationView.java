package com.cobre.notification_service.selfservice.domain.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationView {
    private String eventId;
    private String eventType;
    private String content;
    private LocalDateTime deliveryDate;
    private String clientId;
    private String status;
    private int retryCount;
    private LocalDateTime nextRetryAt;
}
