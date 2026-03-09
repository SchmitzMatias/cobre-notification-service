package com.cobre.notification_service.delivery.application.service;

import com.cobre.notification_service.delivery.application.ports.out.NotificationRepositoryPort;
import com.cobre.notification_service.delivery.application.ports.out.SubscriptionRepositoryPort;
import com.cobre.notification_service.delivery.application.ports.out.WebhookPort;
import com.cobre.notification_service.delivery.domain.models.Notification;
import com.cobre.notification_service.delivery.domain.models.NotificationStatus;
import com.cobre.notification_service.delivery.domain.models.WebhookSubscription;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepository;

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private WebhookPort webhookPort;

    @InjectMocks
    private DeliveryService deliveryService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private Notification defaultNotification;
    private final String CLIENT_ID = "CLIENT_TEST";
    private final String WEBHOOK_URL = "https://example.com/webhook";

    @BeforeEach
    void setUp() {
        defaultNotification = Notification.builder()
                .eventId("EVT001")
                .eventType("test_event")
                .content("Test Content")
                .clientId(CLIENT_ID)
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .nextRetryAt(null)
                .build();
    }

    @Test
    void shouldProcessOnlyPendingEventsFromRepository() {
        // Arrange
        when(notificationRepository.findPendingDue(any(LocalDateTime.class)))
                .thenReturn(List.of(defaultNotification));
        when(subscriptionRepository.findByClientId(CLIENT_ID))
                .thenReturn(Optional.of(new WebhookSubscription(CLIENT_ID, WEBHOOK_URL)));

        // Act
        deliveryService.processAllPendingNotifications();

        // Assert
        verify(notificationRepository).findPendingDue(any(LocalDateTime.class));
        verify(webhookPort).send(eq(WEBHOOK_URL), eq("EVT001"), eq("test_event"), eq("Test Content"));
    }

    @Test
    void shouldMarkAsCompletedAndSetDeliveryDateOnSuccessfulWebhook() {
        // Arrange
        LocalDateTime expectedDeliveryDate = LocalDateTime.of(2026, 1, 1, 12, 0);

        when(notificationRepository.findPendingDue(any(LocalDateTime.class)))
                .thenReturn(List.of(defaultNotification));
        when(subscriptionRepository.findByClientId(CLIENT_ID))
                .thenReturn(Optional.of(new WebhookSubscription(CLIENT_ID, WEBHOOK_URL)));
        when(webhookPort.send(eq(WEBHOOK_URL), anyString(), anyString(), anyString()))
                .thenReturn(expectedDeliveryDate);

        // Act
        deliveryService.processAllPendingNotifications();

        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedObject = notificationCaptor.getValue();

        assertThat(savedObject.getStatus()).isEqualTo(NotificationStatus.COMPLETED);
        assertThat(savedObject.getDeliveryDate()).isEqualTo(expectedDeliveryDate);
    }

    @Test
    void shouldMarkAsFailedWhenNoActiveSubscriptionIsFound() {
        // Arrange
        when(notificationRepository.findPendingDue(any(LocalDateTime.class)))
                .thenReturn(List.of(defaultNotification));
        when(subscriptionRepository.findByClientId(CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act
        deliveryService.processAllPendingNotifications();

        // Assert
        verify(webhookPort, never()).send(anyString(), anyString(), anyString(), anyString());

        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedObject = notificationCaptor.getValue();

        assertThat(savedObject.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void shouldIncrementRetryCountAndScheduleNextRetryOnWebhookFailure() {
        // Arrange
        when(notificationRepository.findPendingDue(any(LocalDateTime.class)))
                .thenReturn(List.of(defaultNotification));
        when(subscriptionRepository.findByClientId(CLIENT_ID))
                .thenReturn(Optional.of(new WebhookSubscription(CLIENT_ID, WEBHOOK_URL)));
        when(webhookPort.send(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("503 Service Unavailable"));

        // Act
        deliveryService.processAllPendingNotifications();

        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedObject = notificationCaptor.getValue();

        // Status should remain PENDING
        assertThat(savedObject.getStatus()).isEqualTo(NotificationStatus.PENDING);
        // Retry count should increment to 1
        assertThat(savedObject.getRetryCount()).isEqualTo(1);
        // Next retry should be calculated as now + 2^1 minutes
        assertThat(savedObject.getNextRetryAt()).isNotNull();
    }

    @Test
    void shouldMarkAsFailedWhenMaxRetriesReached() {
        // Arrange
        defaultNotification.setRetryCount(DeliveryService.MAX_RETRIES - 1);

        when(notificationRepository.findPendingDue(any(LocalDateTime.class)))
                .thenReturn(List.of(defaultNotification));
        when(subscriptionRepository.findByClientId(CLIENT_ID))
                .thenReturn(Optional.of(new WebhookSubscription(CLIENT_ID, WEBHOOK_URL)));
        when(webhookPort.send(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("WebhookTimeout"));

        // Act
        deliveryService.processAllPendingNotifications();

        // Assert
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedObject = notificationCaptor.getValue();

        // Retries crossed threshold, so it should be FAILED
        assertThat(savedObject.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedObject.getRetryCount()).isEqualTo(DeliveryService.MAX_RETRIES);
        assertThat(savedObject.getNextRetryAt()).isNull();
    }
}
