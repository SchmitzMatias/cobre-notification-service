package com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence;

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
public class NotificationQueryEntity {

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

    @Column(name = "status")
    private String status;
}
