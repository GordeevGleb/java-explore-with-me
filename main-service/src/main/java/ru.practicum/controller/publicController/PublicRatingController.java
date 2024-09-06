package ru.practicum.controller.publicController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.rating.OutRatingShortDto;
import ru.practicum.service.rating.RatingService;

import java.util.List;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class PublicRatingController {

    private final RatingService ratingService;

    @GetMapping("/params")
    public List<EventShortDto> getByRatingParams(@RequestParam(required = false, defaultValue = "0.0f") Float locationRate,
                                                 @RequestParam(required = false, defaultValue = "0.0f") Float organizationRate,
                                                 @RequestParam(required = false, defaultValue = "0.0f") Float contentRate) {
        return ratingService.getByRatingParams(locationRate, organizationRate, contentRate);
    }

    @GetMapping("/{eventId}")
    public List<OutRatingShortDto> getByEventId(@PathVariable Long eventId) {
        return ratingService.getByEventId(eventId);
    }
}
