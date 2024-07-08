package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    EndpointHit toEndpointHit(EndpointHitRequestDto dto);

    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);

    List<ViewStatsDto> toListViewStatsDto(List<ViewStats> viewStatsList);

    ViewStatsDto toViewStatsDto(ViewStats viewStats);
}
