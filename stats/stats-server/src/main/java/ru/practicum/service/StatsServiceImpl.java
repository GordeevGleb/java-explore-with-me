package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.EndpointHitResponseDto;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.exception.DateTimeException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;
import ru.practicum.util.StatsDateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

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
        log.info("STATS SERVER LOG: endpointHit posted");
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
        List<ViewStatsResponseDto> resultList;
        if (uris != null) {
            resultList = unique.equals(Boolean.TRUE) ? statsMapper
                    .toListViewStatsResponseDto(statsRepository.findUniqueByUris(startTime, endTime, uris)) :
                    statsMapper.toListViewStatsResponseDto(statsRepository.findByUris(startTime,endTime, uris));
        } else {
            resultList = unique.equals(Boolean.TRUE) ? statsMapper
                    .toListViewStatsResponseDto(statsRepository.findUnique(startTime,endTime)) :
                    statsMapper.toListViewStatsResponseDto(statsRepository.findAll(startTime, endTime));
        }
        log.info("params: start " + start + " end " + end + " uris " + uris + " unique " + unique);
        log.info("STATS SERVER LOG: stats list formed");
        return resultList;
    }
}

