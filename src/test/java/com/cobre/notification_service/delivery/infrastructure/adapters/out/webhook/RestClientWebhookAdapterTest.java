package com.cobre.notification_service.delivery.infrastructure.adapters.out.webhook;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@RestClientTest(RestClientWebhookAdapter.class)
class RestClientWebhookAdapterTest {

    @Autowired
    private RestClientWebhookAdapter adapter;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldReturnExtractedDateOnSuccessfulWebhookWithDateHeader() {
        // Arrange
        String url = "https://example.com/webhook";
        ZonedDateTime expectedTime = ZonedDateTime.of(2026, 3, 10, 10, 0, 0, 0, ZoneId.of("GMT"));
        String dateHeaderValue = expectedTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.OK)
                        .header("Date", dateHeaderValue));

        // Act
        LocalDateTime result = adapter.send(url, "EVT001", "test_type", "some content");

        // Assert
        server.verify();
        // The date returned is converted to system default zone, so we compare the
        // underlying epoch second
        assertThat(result.atZone(ZoneId.systemDefault()).toEpochSecond())
                .isEqualTo(expectedTime.toEpochSecond());
    }

    @Test
    void shouldReturnCurrentNowOnSuccessfulWebhookWithoutDateHeader() {
        // Arrange
        String url = "https://example.com/webhook";

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)); // No Date header

        // Act
        LocalDateTime beforeCall = LocalDateTime.now();
        LocalDateTime result = adapter.send(url, "EVT001", "test_type", "some content");
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        server.verify();
        // Since it falls back to LocalDataTime.now(), the result should be between
        // beforeCall and afterCall
        assertThat(result).isBetween(beforeCall.minusSeconds(1), afterCall.plusSeconds(1));
    }

    @Test
    void shouldThrowExceptionOnNon2xxResponse() {
        // Arrange
        String url = "https://example.com/fail";

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(Exception.class, () -> adapter.send(url, "EVT002", "type", "content"));
        server.verify();
    }
}
