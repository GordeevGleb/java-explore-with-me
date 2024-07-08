package ru.practicum.service;

import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHitDto post(EndpointHitRequestDto endpointHitRequestDto);

    List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
