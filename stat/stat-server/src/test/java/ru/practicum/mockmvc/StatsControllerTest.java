package ru.practicum.mockmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.EventHitDto;
import ru.practicum.EventStatsResponseDto;
import ru.practicum.controller.StatsController;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    private final String invalidDateTimeFormat = "2023-01-01T00:00:00";
    private final String validApp = "ewm-main-service";
    private final String validUri = "/events/1";
    private final String validIp = "192.168.1.1";
    private String validStartFormat;
    private String validEndFormat;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatsService statsService;

    private DateTimeFormatter formatter;

    @Value("${explore-with-me.datetime.format}")
    public void setFormatter(String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @PostConstruct
    void setup() {
        validStartFormat = LocalDateTime.of(2023, 1, 1, 0, 0, 0).format(formatter);
        validEndFormat = LocalDateTime.of(2023, 1, 2, 0, 0, 0).format(formatter);
    }


    // ==================== POST /hit Tests ====================

    @Test
    void hit_ValidEventHitDto_ShouldReturnCreated() throws Exception {
        // Given
        EventHitDto validEventHit = EventHitDto.builder()
                .app(validApp)
                .uri(validUri)
                .ip(validIp)
                .timestamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .build();

        doNothing().when(statsService).hit(any(EventHitDto.class));

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEventHit)))
                .andExpect(status().isCreated());

        verify(statsService, times(1)).hit(any(EventHitDto.class));
    }

    @Test
    void hit_NullApp_ShouldReturnBadRequest() throws Exception {
        // Given
        EventHitDto eventHitWithNullApp = EventHitDto.builder()
                .app(null)
                .uri(validUri)
                .ip(validIp)
                .timestamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .build();

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventHitWithNullApp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Field 'app' should not be null"));

        verify(statsService, never()).hit(any(EventHitDto.class));
    }

    @Test
    void hit_NullUri_ShouldReturnBadRequest() throws Exception {
        // Given
        EventHitDto eventHitWithNullUri = EventHitDto.builder()
                .app(validApp)
                .uri(null)
                .ip(validIp)
                .timestamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .build();

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventHitWithNullUri)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Field 'uri' should not be null"));

        verify(statsService, never()).hit(any(EventHitDto.class));
    }

    @Test
    void hit_NullIp_ShouldReturnBadRequest() throws Exception {
        // Given
        EventHitDto eventHitWithNullIp = EventHitDto.builder()
                .app(validApp)
                .uri(validUri)
                .ip(null)
                .timestamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .build();

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventHitWithNullIp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Field 'ip' should not be null"));

        verify(statsService, never()).hit(any(EventHitDto.class));
    }

    @Test
    void hit_NullTimestamp_ShouldReturnBadRequest() throws Exception {
        // Given
        EventHitDto eventHitWithNullTimestamp = EventHitDto.builder()
                .app(validApp)
                .uri(validUri)
                .ip(validIp)
                .timestamp(null)
                .build();

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventHitWithNullTimestamp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Field 'timestamp' should not be null"));

        verify(statsService, never()).hit(any(EventHitDto.class));
    }

    @Test
    void hit_EmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).hit(any(EventHitDto.class));
    }

    @Test
    void hit_InvalidJsonFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).hit(any(EventHitDto.class));
    }

    // ==================== GET /stats Tests ====================

    @Test
    void stats_ValidParametersWithoutUris_ShouldReturnStats() throws Exception {
        // Given
        List<EventStatsResponseDto> expectedStats = Arrays.asList(
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri(validUri)
                        .hits(10L)
                        .build(),
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri("/events/2")
                        .hits(5L)
                        .build()
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].app").value(validApp))
                .andExpect(jsonPath("$[0].uri").value(validUri))
                .andExpect(jsonPath("$[0].hits").value(10))
                .andExpect(jsonPath("$[1].app").value(validApp))
                .andExpect(jsonPath("$[1].uri").value("/events/2"))
                .andExpect(jsonPath("$[1].hits").value(5));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(false));
    }

    @Test
    void stats_ValidParametersWithUris_ShouldReturnStats() throws Exception {
        // Given
        List<EventStatsResponseDto> expectedStats = Collections.singletonList(
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri(validUri)
                        .hits(15L)
                        .build()
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(List.class), anyBoolean()))
                .thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("uris", "/events/1", "/events/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].app").value(validApp))
                .andExpect(jsonPath("$[0].uri").value(validUri))
                .andExpect(jsonPath("$[0].hits").value(15));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class),
                eq(false));
    }

    @Test
    void stats_UniqueParameterTrue_ShouldReturnStats() throws Exception {
        // Given
        List<EventStatsResponseDto> expectedStats = Collections.singletonList(
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri(validUri)
                        .hits(8L)
                        .build()
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hits").value(8));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(true));
    }

    @Test
    void stats_UniqueParameterFalse_ShouldReturnStats() throws Exception {
        // Given
        List<EventStatsResponseDto> expectedStats = Collections.singletonList(
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri(validUri)
                        .hits(20L)
                        .build()
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hits").value(20));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(false));
    }

    @Test
    void stats_UniqueParameterCaseInsensitive_ShouldReturnStats() throws Exception {
        // Given
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        // When & Then - Test different cases
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("unique", "TRUE"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("unique", "False"))
                .andExpect(status().isOk());

        verify(statsService, times(2)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_MissingStartParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/stats")
                        .param("end", validEndFormat))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_MissingEndParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_InvalidStartDateFormat_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", invalidDateTimeFormat)
                        .param("end", validEndFormat))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_InvalidEndDateFormat_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", invalidDateTimeFormat))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_InvalidUniqueParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("unique", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal Argument"));

        verify(statsService, never()).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_EmptyUrisList_ShouldReturnStats() throws Exception {
        // Given
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("uris", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class),
                eq(false));
    }

    @Test
    void stats_MultipleUris_ShouldReturnStats() throws Exception {
        // Given
        List<EventStatsResponseDto> expectedStats = Arrays.asList(
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri("/events/1")
                        .hits(10L)
                        .build(),
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri("/events/2")
                        .hits(5L)
                        .build()
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(List.class), anyBoolean()))
                .thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("uris", "/events/1", "/events/2", "/events/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class),
                eq(false));
    }

    @Test
    void stats_ServiceReturnsEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                eq(false));
    }

    @Test
    void stats_ServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenThrow(new RuntimeException("Very bad error"));

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat))
                .andExpect(status().isInternalServerError());

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    // ==================== Edge Cases and Integration Tests ====================

    @Test
    void stats_BoundaryDateValues_ShouldReturnStats() throws Exception {
        // Given
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        // When & Then - Test with same start and end dates
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validStartFormat))
                .andExpect(status().isOk());

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(),
                anyBoolean());
    }

    @Test
    void stats_AllParametersProvided_ShouldReturnStats() throws Exception {
        // Given
        List<EventStatsResponseDto> expectedStats = Collections.singletonList(
                EventStatsResponseDto.builder()
                        .app(validApp)
                        .uri(validUri)
                        .hits(3L)
                        .build()
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(List.class), anyBoolean()))
                .thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/stats")
                        .param("start", validStartFormat)
                        .param("end", validEndFormat)
                        .param("uris", "/events/1")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hits").value(3));

        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class),
                eq(true));
    }
}