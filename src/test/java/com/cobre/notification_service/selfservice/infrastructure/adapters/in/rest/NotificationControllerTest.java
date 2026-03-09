package com.cobre.notification_service.selfservice.infrastructure.adapters.in.rest;

import com.cobre.notification_service.selfservice.application.ports.in.QueryNotificationsUseCase;
import com.cobre.notification_service.selfservice.application.ports.in.ReplayNotificationUseCase;
import com.cobre.notification_service.selfservice.domain.models.NotificationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryNotificationsUseCase queryUseCase;

    @MockBean
    private ReplayNotificationUseCase replayUseCase;

    private NotificationView defaultView;

    // A dummy configuration so Spring context loads GlobalExceptionHandler if we
    // want
    @TestConfiguration
    static class ControllerTestConfig {
    }

    @BeforeEach
    void setUp() {
        defaultView = NotificationView.builder()
                .eventId("EVT001")
                .eventType("card_payment")
                .content("Paid 100")
                .deliveryDate(LocalDateTime.of(2026, 3, 15, 12, 0))
                .clientId("CLIENT001")
                .status("COMPLETED")
                .retryCount(0)
                .nextRetryAt(null)
                .build();
    }

    @Test
    void shouldReturn200AndJsonArrayWhenQueryingNotifications() throws Exception {
        // Arrange
        LocalDate queryDate = LocalDate.of(2026, 3, 15);
        when(queryUseCase.findAll("CLIENT001", "COMPLETED", queryDate))
                .thenReturn(List.of(defaultView));

        // Act & Assert
        mockMvc.perform(get("/api/notification_events")
                .param("clientId", "CLIENT001")
                .param("status", "COMPLETED")
                .param("deliveryDate", "2026-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].eventId").value("EVT001"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(queryUseCase).findAll("CLIENT001", "COMPLETED", queryDate);
    }

    @Test
    void shouldReturn200AndJsonObjectWhenQueryingSingleNotification() throws Exception {
        // Arrange
        when(queryUseCase.findById("EVT001")).thenReturn(defaultView);

        // Act & Assert
        mockMvc.perform(get("/api/notification_events/EVT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("EVT001"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(queryUseCase).findById("EVT001");
    }

    @Test
    void shouldReturn202AcceptedWhenReplayingEvent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/notification_events/EVT009/replay"))
                .andExpect(status().isAccepted());

        verify(replayUseCase).replay("EVT009");
    }
}
