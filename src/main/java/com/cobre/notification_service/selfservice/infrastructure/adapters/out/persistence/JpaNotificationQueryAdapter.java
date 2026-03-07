package com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.selfservice.application.ports.out.NotificationQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaNotificationQueryAdapter implements NotificationQueryPort {

    private final JpaNotificationQueryRepository jpaRepository;

    @Override
    public List<NotificationQueryEntity> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<NotificationQueryEntity> findById(String eventId) {
        return jpaRepository.findById(eventId);
    }

    @Override
    public List<NotificationQueryEntity> findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId);
    }
}
