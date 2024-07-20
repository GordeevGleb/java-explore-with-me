package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.EndpointHitResponseDto;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;
import ru.practicum.exception.DateTimeException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;
import ru.practicum.util.StatsDateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    private final StatsMapper statsMapper;

    private final StatsDateTimeFormatter statsDateTimeFormatter;

    @Override
    public EndpointHitResponseDto postHit(EndpointHitRequestDto endpointHitRequestDto) {
        log.info("STATS SERVER LOG: postHit");
        EndpointHit actual = statsMapper.toEndpointHit(endpointHitRequestDto);
        statsRepository.save(actual);
        return statsMapper.toEndpointHitResponseDto(actual);
    }

    @Override
    public List<ViewStatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("STATS SERVER LOG: getStats");
        LocalDateTime startTime = statsDateTimeFormatter.format(start);
        LocalDateTime endTime = statsDateTimeFormatter.format(end);
        if (endTime.isBefore(startTime)) {
            throw new DateTimeException("Date time exception");
        }
        List<ViewStatsResponseDto> resultList = new ArrayList<>();
        if (uris != null) {
            if (unique) {
                resultList = statsMapper
                        .toListViewStatsResponseDto
                                (statsRepository.findEndpointHitsWithUniqueIpWithUris(startTime, endTime, uris));
            }
            resultList = statsMapper
                    .toListViewStatsResponseDto(statsRepository.findEndpointHitsWithUris(startTime,endTime, uris));
        } else {
            if (unique) {
                resultList = statsMapper
                        .toListViewStatsResponseDto(statsRepository.findEndpointHitsWithUniqueIp(startTime,endTime));
            }
            resultList = statsMapper.toListViewStatsResponseDto(statsRepository.findEndpointHits(startTime, endTime));
        }
        log.info("STATS SERVER LOG: stats list formed");
        return resultList;
    }
}

