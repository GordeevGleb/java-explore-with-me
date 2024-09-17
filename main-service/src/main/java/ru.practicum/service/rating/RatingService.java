package ru.practicum.service.rating;

import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.rating.*;
import ru.practicum.entity.Event;
import ru.practicum.enums.QueryLikeParam;

import java.util.List;

public interface RatingService {

    OutRatingDto create(Long userId, Long eventId, IncRatingDto incRatingDto);

    OutRatingDto update(Long userId, Long ratingId, IncRatingDto incRatingDto);

    OutRatingDto updateByAdmin(Long ratingId, IncRatingDto incRatingDto);

    void delete(Long ratingId);

    void deleteByUserId(Long userId);

    OutRatingDto getById(Long ratingId);

    List<OutRatingDto> getAllUsersRatings(Long userId, QueryLikeParam queryLikeParam, Integer from, Integer size);

    List<OutRatingDto> getAll(QueryLikeParam queryLikeParam,
                              List<Long> users,
                              List<Long> events,
                              Integer from,
                              Integer size);

    List<OutRatingShortDto> getByEventId(Long eventId);

    List<OutRatingDto> getByUserId(Long userId);

    List<EventShortDto> getByRatingParams(Float locationRate,
                                          Float organizationRate,
                                          Float contentRate);

    EventFullRatingDto calculateEventRating(Event event);

    EventShortRatingDto calculateEventShortRating(Event event);
}
