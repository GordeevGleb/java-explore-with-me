package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.rating.EventFullRatingDto;
import ru.practicum.dto.rating.EventShortRatingDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Rating;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "requestModeration", source = "newEventDto.requestModeration", defaultValue = "true")
    @Mapping(target = "createdOn", source = "localDateTime")
    @Mapping(target = "state", source = "eventState")
    Event toEvent(NewEventDto newEventDto,
                  Category category,
                  EventState eventState,
                  User initiator,
                  LocalDateTime localDateTime,
                  Set<Rating> ratingSet);

//    @Mapping(target = "ratingSet", ignore = true)
//    List<EventShortDto> toEventShortDtoList(List<Event> events);

    @Mapping(target = "confirmedRequests", source = "event.confirmedRequestCount")
    @Mapping(target = "views", source = "event.viewCount")
    @Mapping(target = "eventFullRatingDto", source = "eventFullRatingDto")
    EventFullDto toEventFullDto(Event event, EventFullRatingDto eventFullRatingDto);

    @Mapping(target = "eventShortRatingDto", source = "eventShortRatingDto")
    EventShortDto toEventShortDto(Event event, EventShortRatingDto eventShortRatingDto);

//    @Mapping(target = "confirmedRequests", source = "event.confirmedRequestCount")
//    @Mapping(target = "views", source = "event.viewCount")
//    @Mapping(target = "ratingSet", ignore = true)
//    List<EventFullDto> toEventFullDtoList(List<Event> events);
}
