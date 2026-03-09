package com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.selfservice.application.ports.out.NotificationQueryPort;
import com.cobre.notification_service.selfservice.domain.models.NotificationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaNotificationQueryAdapter implements NotificationQueryPort {

    private final JpaNotificationQueryRepository jpaRepository;

    @Override
public List<NotificationView> findAll(String clientId, String status, LocalDate deliveryDate) {
    Specification<NotificationQueryEntity> spec = Specification.where((root, query, cb) -> cb.conjunction());

    if (clientId != null && !clientId.isBlank()) {
        spec = spec.and((root, query, cb) -> cb.equal(root.get("clientId"), clientId));
    }

    if (status != null && !status.isBlank()) {
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
    }

    if (deliveryDate != null) {
        LocalDateTime startOfDay = deliveryDate.atStartOfDay();
        LocalDateTime endOfDay = deliveryDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        spec = spec.and((root, query, cb) -> cb.between(root.get("deliveryDate"), startOfDay, endOfDay));
    }

    // JPA Repository maneja perfectamente findAll(null) devolviendo todos los registros
    return jpaRepository.findAll(spec).stream()
            .map(this::toDomain)
            .toList();
}

    @Override
    public Optional<NotificationView> findById(String eventId) {
        return jpaRepository.findById(eventId).map(this::toDomain);
    }

    @Override
    public void save(NotificationView view) {
        jpaRepository.save(toEntity(view));
    }

    private NotificationQueryEntity toEntity(NotificationView domain) {
        return NotificationQueryEntity.builder()
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

    private NotificationView toDomain(NotificationQueryEntity entity) {
        return NotificationView.builder()
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
