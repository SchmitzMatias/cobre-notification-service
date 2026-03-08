package com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, String> {

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE n.status = :status
              AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)
            """)
    List<NotificationEntity> findPendingDue(
            @Param("status") NotificationStatus status,
            @Param("now") LocalDateTime now);
}
