package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

private final StatsRepository statsRepository;
private final StatsMapper statsMapper;

    @Override
    public EndpointHitDto post(EndpointHitRequestDto endpointHitRequestDto) {
        EndpointHit actual = statsMapper.toEndpointHit(endpointHitRequestDto);
        statsRepository.save(actual);
        log.info("STAT SERVER LOG: endpointHit created;");
        EndpointHitDto result = statsMapper.toEndpointHitDto(actual);
        return result;
    }

    @Override
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (Boolean.TRUE.equals(unique)) {
            List<ViewStats> actual = statsRepository.findStatsByDatesUniqueIp(start, end, uris);
            log.info("STAT SERVER LOG: stat list with unique ip-s formed");
            List<ViewStatsDto> result = statsMapper.toListViewStatsDto(actual);
            return result;
        }
        List<ViewStats> actual = statsRepository.findStatsByDates(start, end, uris);
        log.info("STAT SERVER LOG: stat list without unique ip-s formed");
        List<ViewStatsDto> result = statsMapper.toListViewStatsDto(actual);
        return result;
    }
}
