package com.cobre.notification_service.selfservice.infrastructure.adapters.in.rest;

import com.cobre.notification_service.selfservice.application.ports.in.QueryNotificationsUseCase;
import com.cobre.notification_service.selfservice.application.ports.in.ReplayNotificationUseCase;
import com.cobre.notification_service.selfservice.domain.models.NotificationView;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notification_events")
@RequiredArgsConstructor
public class NotificationController {

    private final QueryNotificationsUseCase queryUseCase;
    private final ReplayNotificationUseCase replayUseCase;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate) {

        return queryUseCase.findAll(clientId, status, deliveryDate).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public NotificationResponse getNotification(@PathVariable String id) {
        return mapToResponse(queryUseCase.findById(id));
    }

    @PostMapping("/{id}/replay")
    public ResponseEntity<Void> replayNotification(@PathVariable String id) {
        replayUseCase.replay(id);
        return ResponseEntity.accepted().build();
    }

    private NotificationResponse mapToResponse(NotificationView view) {
        return NotificationResponse.builder()
                .eventId(view.getEventId())
                .eventType(view.getEventType())
                .content(view.getContent())
                .deliveryDate(view.getDeliveryDate())
                .clientId(view.getClientId())
                .status(view.getStatus())
                .build();
    }
}
