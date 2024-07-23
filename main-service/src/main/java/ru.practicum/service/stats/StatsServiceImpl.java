package ru.practicum.service.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.Event;
import ru.practicum.exception.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsClient statsClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendStat(Event event, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String nameService = "main-service";

        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(now.format(dateFormatter))
                .uri(request.getRequestURI())
                .app(nameService)
                .ip(request.getRemoteAddr())
                .build();
        statsClient.addStats(requestDto);
    }

    @Override
    public void sendStat(List<Event> events, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String nameService = "main-service";
        EndpointHitRequestDto requestDto = EndpointHitRequestDto.builder()
                .timestamp(now.format(dateFormatter))
                .uri(request.getRequestURI())
                .app(nameService)
                .ip(request.getRemoteAddr())
                .build();
        statsClient.addStats(requestDto);
    }

    @Override
    public List<ViewStatsResponseDto> getStats(String startTime, String endTime, List<String> uris) {
        return statsClient.getStats(startTime, endTime, uris, false);
    }
}
