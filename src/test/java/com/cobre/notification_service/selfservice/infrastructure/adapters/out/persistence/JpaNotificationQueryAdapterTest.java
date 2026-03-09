package com.cobre.notification_service.selfservice.infrastructure.adapters.out.persistence;

import com.cobre.notification_service.selfservice.domain.models.NotificationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaNotificationQueryAdapterTest {

    @Mock
    private JpaNotificationQueryRepository jpaRepository;

    @InjectMocks
    private JpaNotificationQueryAdapter adapter;

    private NotificationQueryEntity entity;

    @BeforeEach
    void setUp() {
        entity = NotificationQueryEntity.builder()
                .eventId("EVT001")
                .eventType("test_event")
                .content("content")
                .deliveryDate(LocalDateTime.now())
                .clientId("CLIENT001")
                .status("COMPLETED")
                .retryCount(0)
                .nextRetryAt(null)
                .build();
    }

    @Test
    void shouldFindAllWithNullFilters() {
        // Arrange
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(List.of(entity));

        // Act
        List<NotificationView> results = adapter.findAll(null, null, null);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEventId()).isEqualTo("EVT001");
        
        // Verifies the Specification logic executes and invokes the repository
        verify(jpaRepository).findAll(any(Specification.class));
    }

    @Test
    void shouldFindAllWithSpecificFilters() {
        // Arrange
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(List.of(entity));

        // Act
        List<NotificationView> results = adapter.findAll("CLIENT001", "COMPLETED", LocalDate.now());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getClientId()).isEqualTo("CLIENT001");
        assertThat(results.get(0).getStatus()).isEqualTo("COMPLETED");

        verify(jpaRepository).findAll(any(Specification.class));
    }

    @Test
    void shouldFindByIdAndMapToDomain() {
        // Arrange
        when(jpaRepository.findById("EVT001")).thenReturn(java.util.Optional.of(entity));

        // Act
        java.util.Optional<NotificationView> result = adapter.findById("EVT001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEventId()).isEqualTo("EVT001");
        verify(jpaRepository).findById("EVT001");
    }

    @Test
    void shouldSaveAndMapToEntity() {
        // Arrange
        NotificationView view = NotificationView.builder()
                .eventId("EVT001")
                .status("FAILED")
                .build();

        // Act
        adapter.save(view);

        // Assert
        org.mockito.ArgumentCaptor<NotificationQueryEntity> entityCaptor = org.mockito.ArgumentCaptor
                .forClass(NotificationQueryEntity.class);
        verify(jpaRepository).save(entityCaptor.capture());

        NotificationQueryEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getEventId()).isEqualTo("EVT001");
        assertThat(savedEntity.getStatus()).isEqualTo("FAILED");
    }
}
