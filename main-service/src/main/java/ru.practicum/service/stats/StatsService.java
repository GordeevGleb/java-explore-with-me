package ru.practicum.service.stats;

import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void sendStat(Event event, HttpServletRequest request);

    void sendStat(List<Event> events, HttpServletRequest request);

    List<ViewStatsResponseDto> getStats(String startTime, String endTime, List<String> uris);

}
