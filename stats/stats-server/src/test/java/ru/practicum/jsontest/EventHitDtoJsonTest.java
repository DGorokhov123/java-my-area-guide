package ru.practicum.jsontest;

import jakarta.annotation.PostConstruct;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.env.Environment;
import ru.practicum.EventHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Выполнять только при запущенных Discovery and Config servers")
@JsonTest
class EventHitDtoJsonTest {

    @Autowired
    private JacksonTester<EventHitDto> json;

    @Autowired
    private Environment environment;

    private DateTimeFormatter formatter;

    @PostConstruct
    void setup() {
        String dateTimeFormat = environment.getProperty("explore-with-me.datetime.format");
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Test
    void shouldSerializeEventHitDto() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 7, 15, 14, 30, 45);
        EventHitDto eventHit = EventHitDto.builder()
                .app("main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(timestamp)
                .build();

        // When & Then
        assertThat(json.write(eventHit)).isStrictlyEqualToJson("{" + """
                    "app": "main-service",
                    "uri": "/events/1",
                    "ip": "192.168.1.1",
                    "timestamp": "FORMATTED"
                }
                """.replace("FORMATTED", timestamp.format(formatter)));
    }

    @Test
    void shouldDeserializeEventHitDto() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 7, 15, 14, 30, 45);
        String jsonContent = "{" + """
                    "app": "main-service",
                    "uri": "/events/1",
                    "ip": "192.168.1.1",
                    "timestamp": "FORMATTED"
                }
                """.replace("FORMATTED", timestamp.format(formatter));

        // When
        EventHitDto eventHit = json.parse(jsonContent).getObject();

        // Then
        Assertions.assertThat(eventHit.getApp()).isEqualTo("main-service");
        Assertions.assertThat(eventHit.getUri()).isEqualTo("/events/1");
        Assertions.assertThat(eventHit.getIp()).isEqualTo("192.168.1.1");
        Assertions.assertThat(eventHit.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldSerializeWithDifferentDateTimeFormat() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 12, 25, 23, 59, 59);
        EventHitDto eventHit = EventHitDto.builder()
                .app("analytics-service")
                .uri("/api/stats")
                .ip("10.0.0.1")
                .timestamp(timestamp)
                .build();

        // When & Then
        assertThat(json.write(eventHit)).hasJsonPath("$.timestamp")
                .extractingJsonPathStringValue("$.timestamp")
                .isEqualTo(timestamp.format(formatter));
    }

    @Test
    void shouldDeserializeWithSpecialCharacters() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        String jsonContent = "{" + """
                    "app": "test-app",
                    "uri": "/events/search?query=test&eventSort=date",
                    "ip": "127.0.0.1",
                    "timestamp": "FORMATTED"
                }
                """.replace("FORMATTED", timestamp.format(formatter));

        // When
        EventHitDto eventHit = json.parse(jsonContent).getObject();

        // Then
        Assertions.assertThat(eventHit.getApp()).isEqualTo("test-app");
        Assertions.assertThat(eventHit.getUri()).isEqualTo("/events/search?query=test&eventSort=date");
        Assertions.assertThat(eventHit.getIp()).isEqualTo("127.0.0.1");
        Assertions.assertThat(eventHit.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldDeserializeTimestampCorrectly() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 3, 15, 16, 45, 22);
        String jsonContent = "{" + """
                    "app": "web-app",
                    "uri": "/",
                    "ip": "203.0.113.1",
                    "timestamp": "FORMATTED"
                }
                """.replace("FORMATTED", timestamp.format(formatter));

        // When
        EventHitDto eventHit = json.parse(jsonContent).getObject();

        // Then
        Assertions.assertThat(eventHit.getTimestamp())
                .isEqualTo(timestamp);
    }

    @Test
    void shouldSerializeAllFieldsCorrectly() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2024, 8, 20, 12, 0, 0);
        EventHitDto eventHit = EventHitDto.builder()
                .app("integration-test")
                .uri("/api/v1/events/123")
                .ip("172.16.0.1")
                .timestamp(timestamp)
                .build();

        // When & Then
        assertThat(json.write(eventHit))
                .hasJsonPath("$.app").extractingJsonPathStringValue("$.app").isEqualTo("integration-test");
        assertThat(json.write(eventHit))
                .hasJsonPath("$.uri").extractingJsonPathStringValue("$.uri").isEqualTo("/api/v1/events/123");
        assertThat(json.write(eventHit))
                .hasJsonPath("$.ip").extractingJsonPathStringValue("$.ip").isEqualTo("172.16.0.1");
        assertThat(json.write(eventHit))
                .hasJsonPath("$.timestamp").extractingJsonPathStringValue("$.timestamp").isEqualTo(timestamp.format(formatter));
    }

    @Test
    void shouldRoundTripSerializationAndDeserialization() throws Exception {
        // Given
        EventHitDto original = EventHitDto.builder()
                .app("round-trip-test")
                .uri("/test/roundtrip")
                .ip("192.168.1.100")
                .timestamp(LocalDateTime.of(2024, 5, 10, 15, 30, 45))
                .build();

        // When
        String jsonString = json.write(original).getJson();
        EventHitDto deserialized = json.parse(jsonString).getObject();

        // Then
        Assertions.assertThat(deserialized).isEqualTo(original);
        Assertions.assertThat(deserialized.getApp()).isEqualTo(original.getApp());
        Assertions.assertThat(deserialized.getUri()).isEqualTo(original.getUri());
        Assertions.assertThat(deserialized.getIp()).isEqualTo(original.getIp());
        Assertions.assertThat(deserialized.getTimestamp()).isEqualTo(original.getTimestamp());
    }

}