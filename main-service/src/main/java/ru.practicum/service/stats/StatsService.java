package ru.practicum.service.stats;

import ru.practicum.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface StatsService {
    void sendStat(HttpServletRequest request);

    Map<Long, Long> getView(List<Event> events);
}
