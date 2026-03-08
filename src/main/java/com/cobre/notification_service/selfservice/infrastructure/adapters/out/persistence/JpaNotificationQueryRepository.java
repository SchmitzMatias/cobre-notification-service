package com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationQueryRepository
        extends JpaRepository<NotificationQueryEntity, String>, JpaSpecificationExecutor<NotificationQueryEntity> {
}
