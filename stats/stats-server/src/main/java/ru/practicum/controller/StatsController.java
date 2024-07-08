package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;


    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto post(@RequestBody @Valid EndpointHitRequestDto endpointHitRequestDto) {
        log.info("STAT SERVER LOG: endpointHit creating");
        return statsService.post(endpointHitRequestDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> get(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                  @RequestParam List<String> uris,
                                  @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("STAT SERVER LOG: get");
        return statsService.get(start, end, uris, unique);
    }
}
