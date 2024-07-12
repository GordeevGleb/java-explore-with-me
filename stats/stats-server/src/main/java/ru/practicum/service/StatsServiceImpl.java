package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.EndpointHitResponseDto;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;
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
        return statsMapper.toEndpointHitResponseDto(actual);
    }

    @Override
    public List<ViewStatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("STATS SERVER LOG: getStats");
        LocalDateTime startTime = statsDateTimeFormatter.format(start);
        LocalDateTime endTime = statsDateTimeFormatter.format(end);
        List<ViewStats> result;
        if (uris != null) {
            if (unique) {
                result = statsRepository.findAllByTimeAndListOfUrisAndUniqueIp(startTime, endTime, uris);
            } else {
                result = statsRepository.findAllByTimeAndListOfUris(startTime, endTime, uris);
            }
        } else if (unique) {
            result = statsRepository.findAllByTimeAndUniqueIp(startTime, endTime);
        } else {
            result = statsRepository.findAllByTime(startTime, endTime);
        }
        return statsMapper.toListViewStatsResponseDto(result);
    }
}
