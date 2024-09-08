package ru.practicum.controller.privateController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.rating.IncRatingDto;
import ru.practicum.dto.rating.OutRatingDto;
import ru.practicum.enums.LikeParam;
import ru.practicum.service.rating.RatingService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/ratings")
@RequiredArgsConstructor
public class PrivateRatingController {

    private final RatingService ratingService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public OutRatingDto create(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody IncRatingDto incRatingDto) {
        return ratingService.create(userId, eventId, incRatingDto);
    }

    @PatchMapping("/{ratingId}")
    public OutRatingDto update(@PathVariable Long userId,
                               @PathVariable Long ratingId,
                               @Valid @RequestBody IncRatingDto incRatingDto) {
        return ratingService.update(userId, ratingId, incRatingDto);
    }

    @GetMapping
    public List<OutRatingDto> getAll(@PathVariable Long userId,
                                     @RequestParam (required = false) LikeParam likeParam,
                                     @RequestParam(required = false, defaultValue = "0") Integer from,
                                     @RequestParam(required = false, defaultValue = "10") Integer size) {
        return ratingService.getAllUsersRatings(userId, likeParam, from, size);
    }
}
