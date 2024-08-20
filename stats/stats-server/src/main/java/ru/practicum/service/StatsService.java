package ru.practicum.service;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsResponseDto;
import java.util.List;

public interface StatsService {

    EndpointHitDto postHit(EndpointHitDto endpointHitDto);

    List<ViewStatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique);

}
