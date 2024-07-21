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

    private final StatsClient statClient;
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
        statClient.addStats(requestDto);
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
        statClient.addStats(requestDto);
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
        statClient.addStats(requestDto);
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
            statClient.addStats(requestDto);
        }
    }

    @Override
    public void setView(Event event) {
        log.info("set views for event " + event.getId() + " " + event.getViews());
        String startTime = event.getCreatedOn().format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());
log.info("uris: " + String.valueOf(uris));
        List<ViewStatsResponseDto> stats = getStats(startTime, endTime, uris);
        log.info("stats size: " + stats.size());
        if (stats.size() > 0) {
            log.info("stats.size == 1");
            event.setViews(stats.get(0).getHits());
        } else {
            log.info("stats size < 1");
            event.setViews(1L);
        }
    }

    @Override
    public List<ViewStatsResponseDto> getStats(String startTime, String endTime, List<String> uris) {
        return statClient.getStats(startTime, endTime, uris, false);
    }
}
