package com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.delivery.application.ports.out.NotificationRepositoryPort;
import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaNotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository jpaRepository;

    @Override
    public NotificationEntity save(NotificationEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public List<NotificationEntity> saveAll(List<NotificationEntity> entities) {
        return jpaRepository.saveAll(entities);
    }

    @Override
    public Optional<NotificationEntity> findById(String eventId) {
        return jpaRepository.findById(eventId);
    }

    @Override
    public List<NotificationEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<NotificationEntity> findPendingDue(LocalDateTime now) {
        return jpaRepository.findPendingDue(NotificationStatus.PENDING, now);
    }
}
