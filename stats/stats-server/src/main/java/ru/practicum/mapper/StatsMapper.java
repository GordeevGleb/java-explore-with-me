package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.EndpointHitResponseDto;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    @Mapping(target = "timestamp", source = "dto.timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EndpointHit toEndpointHit(EndpointHitRequestDto dto);

    EndpointHitResponseDto toEndpointHitResponseDto(EndpointHit entity);

    List<ViewStatsResponseDto> toListViewStatsResponseDto(List<ViewStats> viewStatsList);

    ViewStatsResponseDto toViewStatsResponseDto(ViewStats viewStats);
}
