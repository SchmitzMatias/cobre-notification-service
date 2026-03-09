package com.cobre.notification_service.delivery.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.delivery.domain.models.Notification;
import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaNotificationRepositoryAdapterTest {

    @Mock
    private JpaNotificationRepository jpaRepository;

    @InjectMocks
    private JpaNotificationRepositoryAdapter adapter;

    @Captor
    private ArgumentCaptor<NotificationEntity> entityCaptor;

    @Captor
    private ArgumentCaptor<List<NotificationEntity>> entityListCaptor;

    private Notification defaultDomain;

    @BeforeEach
    void setUp() {
        defaultDomain = Notification.builder()
                .eventId("EVT001")
                .eventType("test")
                .content("content")
                .clientId("CLIENT001")
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .nextRetryAt(null)
                .build();
    }

    @Test
    void shouldSaveMappedEntity() {
        // Act
        adapter.save(defaultDomain);

        // Assert
        verify(jpaRepository).save(entityCaptor.capture());
        NotificationEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getEventId()).isEqualTo("EVT001");
        assertThat(savedEntity.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void shouldSaveAllMappedEntities() {
        // Act
        adapter.saveAll(List.of(defaultDomain));

        // Assert
        verify(jpaRepository).saveAll(entityListCaptor.capture());
        List<NotificationEntity> savedList = entityListCaptor.getValue();

        assertThat(savedList).hasSize(1);
        assertThat(savedList.get(0).getEventId()).isEqualTo("EVT001");
    }

    @Test
    void shouldFindPendingDueAndMapToDomain() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        NotificationEntity entity = NotificationEntity.builder()
                .eventId("EVT002")
                .status(NotificationStatus.PENDING)
                .build();

        when(jpaRepository.findPendingDue(eq(NotificationStatus.PENDING), eq(now)))
                .thenReturn(List.of(entity));

        // Act
        List<Notification> result = adapter.findPendingDue(now);

        // Assert
        verify(jpaRepository).findPendingDue(NotificationStatus.PENDING, now);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventId()).isEqualTo("EVT002");
        assertThat(result.get(0).getStatus()).isEqualTo(NotificationStatus.PENDING);
    }
}
