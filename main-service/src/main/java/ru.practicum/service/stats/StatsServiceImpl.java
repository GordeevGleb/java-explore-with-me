package ru.practicum.service.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsClient statsClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendStat(Event event, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(now.format(dateFormatter))
                .uri("/events/")
                .app(nameService)
                .ip(remoteAddr)
                .build();
        log.info("endpoint: " + requestDto.toString());
        statsClient.addStats(requestDto);
        sendStatForTheEvent(event.getId(), remoteAddr, now, nameService);
    }

    @Override
    public void sendStat(List<Event> events, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(now.format(dateFormatter))
                .uri("/events/")
                .app(nameService)
                .ip(remoteAddr)
                .build();
        statsClient.addStats(requestDto);
        sendStatForEveryEvent(events, remoteAddr, LocalDateTime.now(), nameService);
    }

    @Override
    public void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                                    String nameService) {
        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(now.format(dateFormatter))
                .uri("/events/" + eventId)
                .app(nameService)
                .ip(remoteAddr)
                .build();
        log.info("endpoint: " + requestDto.toString());
        statsClient.addStats(requestDto);
    }

    @Override
    public void sendStatForEveryEvent(List<Event> events, String remoteAddr, LocalDateTime now,
                                      String nameService) {
        for (Event event : events) {
            EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                    .timestamp(now.format(dateFormatter))
                    .uri("/events/" + event.getId())
                    .app(nameService)
                    .ip(remoteAddr)
                    .build();
            statsClient.addStats(requestDto);
        }
    }

    @Override
    public void setView(Event event) {
        log.info("set views for event " + event.getId());
        String startTime = event.getCreatedOn().format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStatsResponseDto> stats = getStats(startTime, endTime, uris);
        if (stats.size() > 0) {
            event.setViews(stats.get(0).getHits());
        } else {
            event.setViews(0L);
        }
    }

    @Override
    public List<ViewStatsResponseDto> getStats(String startTime, String endTime, List<String> uris) {
        return statsClient.getStats(startTime, endTime, uris, false);
    }
}
