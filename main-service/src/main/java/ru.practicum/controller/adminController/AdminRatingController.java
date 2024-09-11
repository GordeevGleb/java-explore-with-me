package ru.practicum.controller.adminController;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.rating.IncRatingDto;
import ru.practicum.dto.rating.OutRatingDto;
import ru.practicum.enums.QueryLikeParam;
import ru.practicum.service.rating.RatingService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/ratings")
@RequiredArgsConstructor
@Validated
public class AdminRatingController {

    private final RatingService ratingService;

    @GetMapping("/{ratingId}")
    public OutRatingDto getById(@PathVariable Long ratingId) {
        return ratingService.getById(ratingId);
    }

    @PatchMapping("/{ratingId}")
    public OutRatingDto updateByAdmin(@PathVariable Long ratingId, @RequestBody IncRatingDto incRatingDto) {
        return ratingService.updateByAdmin(ratingId, incRatingDto);
    }

    @GetMapping("/user/{userId}")
    public List<OutRatingDto> getByUserId(@PathVariable Long userId) {
        return ratingService.getByUserId(userId);
    }

    @DeleteMapping("/{ratingId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long ratingId) {
        ratingService.delete(ratingId);
    }

    @DeleteMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUsersRatings(@PathVariable Long userId) {
        ratingService.deleteByUserId(userId);
    }

    @GetMapping
    public List<OutRatingDto> getAll(@RequestParam(required = false) QueryLikeParam queryLikeParam,
                                     @RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<Long> events,
                                     @RequestParam(required = false, defaultValue = "0") Integer from,
                                     @RequestParam(required = false, defaultValue = "10") Integer size) {
        return ratingService.getAll(queryLikeParam, users, events, from, size);
    }
}
