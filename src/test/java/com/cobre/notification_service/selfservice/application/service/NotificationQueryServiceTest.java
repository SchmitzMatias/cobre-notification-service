package com.cobre.notification_service.selfservice.application.service;

import com.cobre.notification_service.selfservice.application.ports.out.NotificationQueryPort;
import com.cobre.notification_service.selfservice.domain.models.NotificationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock
    private NotificationQueryPort queryPort;

    @InjectMocks
    private NotificationQueryService queryService;

    @Captor
    private ArgumentCaptor<NotificationView> viewCaptor;

    private NotificationView defaultView;

    @BeforeEach
    void setUp() {
        defaultView = NotificationView.builder()
                .eventId("EVT-FAILED")
                .status("FAILED")
                .retryCount(5)
                .nextRetryAt(LocalDateTime.now().minusMinutes(10))
                .build();
    }

    @Test
    void shouldFindAllUsingQueryPort() {
        // Arrange
        LocalDate queryDate = LocalDate.now();
        List<NotificationView> expectedResult = List.of(defaultView);

        when(queryPort.findAll("CLIENT001", "PENDING", queryDate))
                .thenReturn(expectedResult);

        // Act
        List<NotificationView> result = queryService.findAll("CLIENT001", "PENDING", queryDate);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(queryPort).findAll("CLIENT001", "PENDING", queryDate);
    }

    @Test
    void shouldThrowExceptionWhenFindByIdIsNotFound() {
        // Arrange
        when(queryPort.findById("UNKNOWN-ID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> queryService.findById("UNKNOWN-ID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification with id UNKNOWN-ID not found");
    }

    @Test
    void shouldResetStateWhenReplayingNotification() {
        // Arrange
        when(queryPort.findById("EVT-FAILED")).thenReturn(Optional.of(defaultView));

        // Act
        queryService.replay("EVT-FAILED");

        // Assert
        verify(queryPort).save(viewCaptor.capture());
        NotificationView savedView = viewCaptor.getValue();

        assertThat(savedView.getStatus()).isEqualTo("PENDING");
        assertThat(savedView.getRetryCount()).isEqualTo(0);
        assertThat(savedView.getNextRetryAt()).isNull();
    }
}
