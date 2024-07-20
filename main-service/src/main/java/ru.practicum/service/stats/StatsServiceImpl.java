package ru.practicum.service.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsClient statsClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendStat(Event event, HttpServletRequest request) {
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
        sendStatForTheEvent(event.getId(), remoteAddr, now, nameService);
    }

    @Override
    public void sendStat(List<Event> events, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(LocalDateTime.now())
                .uri("/events")
                .app(nameService)
                .ip(request.getRemoteAddr())
                .build();
        statsClient.postHit(requestDto);
        sendStatForEveryEvent(events, remoteAddr, LocalDateTime.now(), nameService);
    }

    @Override
    public void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                                    String nameService) {
        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(now)
                .uri("/events" + eventId)
                .app(nameService)
                .ip(remoteAddr)
                .build();
        statsClient.postHit(requestDto);
    }

    @Override
    public void sendStatForEveryEvent(List<Event> events, String remoteAddr, LocalDateTime now,
                                      String nameService) {
        for (Event event : events) {
            EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                    .timestamp(LocalDateTime.now())
                    .uri("/events" + event.getId())
                    .app(nameService)
                    .ip(remoteAddr)
                    .build();
            statsClient.postHit(requestDto);
        }
    }

    @Override
    public void setView(Event event) {
        String startTime = event.getCreatedOn().format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());

        Object responseBody = statsClient.getStatistics(startTime, endTime, uris, Boolean.TRUE).getBody();
        if (responseBody == null) {
            throw new IllegalArgumentException("Response body is null");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<ViewStatsResponseDto> responseDtos = new ArrayList<>();
        try {
            responseDtos = objectMapper.readValue(
                    objectMapper.writeValueAsString(responseBody),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ViewStatsResponseDto.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ResponseEntity to List<ViewStatsResponseDto>", e);
        }

        if (responseDtos.size() == 0) {
            event.setViews(1L);
        } else {
            event.setViews((long) responseDtos.size());
        }
    }

    @Override
    public List<ViewStatsResponseDto> getStats(String startTime, String endTime, List<String> uris) {
        return (List<ViewStatsResponseDto>) statsClient.getStatistics(startTime, endTime, uris, false).getBody();
    }
}
