package ru.practicum.service;

import ru.practicum.EndpointHitRequestDto;
import ru.practicum.EndpointHitResponseDto;
import ru.practicum.ViewStatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.util.List;

public interface StatsService {

    EndpointHitResponseDto postHit(EndpointHitRequestDto endpointHitRequestDto);

    List<ViewStatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique);

}
