package ru.practicum.service;

import ru.practicum.EndpointHitRequestDto;
import ru.practicum.EndpointHitResponseDto;
import ru.practicum.ViewStatsResponseDto;
import java.util.List;

public interface StatsService {

    EndpointHitResponseDto postHit(EndpointHitRequestDto endpointHitRequestDto);

    List<ViewStatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique);

}
