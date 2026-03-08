package com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.delivery.application.ports.out.NotificationRepositoryPort;
import com.cobre.notification_service.delivery.domain.models.Notification;
import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaNotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository jpaRepository;

    @Override
    public void saveAll(List<Notification> notifications) {
        List<NotificationEntity> entities = notifications.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }

    @Override
    public List<Notification> findPendingDue(LocalDateTime now) {
        return jpaRepository.findPendingDue(NotificationStatus.PENDING, now).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Notification notification) {
        jpaRepository.save(toEntity(notification));
    }

    private NotificationEntity toEntity(Notification domain) {
        return NotificationEntity.builder()
                .eventId(domain.getEventId())
                .eventType(domain.getEventType())
                .content(domain.getContent())
                .deliveryDate(domain.getDeliveryDate())
                .clientId(domain.getClientId())
                .status(domain.getStatus())
                .retryCount(domain.getRetryCount())
                .nextRetryAt(domain.getNextRetryAt())
                .build();
    }

    private Notification toDomain(NotificationEntity entity) {
        return Notification.builder()
                .eventId(entity.getEventId())
                .eventType(entity.getEventType())
                .content(entity.getContent())
                .deliveryDate(entity.getDeliveryDate())
                .clientId(entity.getClientId())
                .status(entity.getStatus())
                .retryCount(entity.getRetryCount())
                .nextRetryAt(entity.getNextRetryAt())
                .build();
    }
}
