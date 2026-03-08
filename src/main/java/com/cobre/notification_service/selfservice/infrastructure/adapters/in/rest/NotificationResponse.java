package com.cobre.notification_service.selfservice.infrastructure.adapters.in.rest;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String eventId;
    private String eventType;
    private String content;
    private LocalDateTime deliveryDate;
    private String clientId;
    private String status;
}
