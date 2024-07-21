package ru.practicum.service.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.Event;
import ru.practicum.exception.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsClient statsClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendStat(HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(LocalDateTime.now())
                .uri("/events")
                .app(nameService)
                .ip(remoteAddr)
                .build();
        statsClient.postHit(requestDto);
    }

    @Override
    public Map<Long, Long> getView(List<Event> events) {
        String startTime = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("date time not found exception"))
                .format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        Object responseBody = statsClient.getStatistics(startTime, endTime, uris, true).getBody();
        if (responseBody == null) {
            throw new IllegalArgumentException("Response body is null");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<Long, Long> result = new HashMap<>();
            List<ViewStatsResponseDto> statsResponseDtos = objectMapper.readValue(
                    objectMapper.writeValueAsString(responseBody),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ViewStatsResponseDto.class)
            );
            statsResponseDtos.stream()
                    .filter(viewStatsResponseDto ->
                            !viewStatsResponseDto.getUri().equals("/events"))
                    .forEach(viewStatsResponseDto ->
                            result.put(Long.parseLong(viewStatsResponseDto.getUri()
                                    .substring("/events".length() + 1)), viewStatsResponseDto.getHits()));
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ResponseEntity to List<ViewStatsResponseDto>", e);
        }
    }
}
