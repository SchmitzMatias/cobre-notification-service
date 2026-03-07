package com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaNotificationQueryRepository extends JpaRepository<NotificationQueryEntity, String> {

    List<NotificationQueryEntity> findByClientId(String clientId);
}
