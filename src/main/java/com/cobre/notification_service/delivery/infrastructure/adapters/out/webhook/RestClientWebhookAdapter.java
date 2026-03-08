package com.cobre.notification_service.delivery.infrastructure.adapters.out.webhook;

import com.cobre.notification_service.delivery.application.ports.out.WebhookPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class RestClientWebhookAdapter implements WebhookPort {

    private final RestClient restClient;

    public RestClientWebhookAdapter() {
        this.restClient = RestClient.builder()
                .requestFactory(buildRequestFactory())
                .build();
    }

    @Override
    public void send(String webhookUrl, String eventId, String eventType, String content) {
        String payload = buildPayload(eventId, eventType, content);
        log.debug("RestClientWebhookAdapter: POST {} | payload={}", webhookUrl, payload);

        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity(); // throws RestClientException on non-2xx
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a simple JSON payload without pulling in a full ObjectMapper
     * dependency.
     * Content is escaped to be safe for JSON strings.
     */
    private String buildPayload(String eventId, String eventType, String content) {
        return String.format(
                "{\"event_id\":\"%s\",\"event_type\":\"%s\",\"content\":\"%s\"}",
                escape(eventId),
                escape(eventType),
                escape(content));
    }

    private String escape(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private org.springframework.http.client.SimpleClientHttpRequestFactory buildRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }
}
