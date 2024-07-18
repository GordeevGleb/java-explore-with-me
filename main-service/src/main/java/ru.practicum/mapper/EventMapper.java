package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "requestModeration", source = "newEventDto.requestModeration", defaultValue = "true")
    @Mapping(target = "createdOn", source = "localDateTime")
    @Mapping(target = "publishedOn", source = "localDateTime")
    @Mapping(target = "state", source = "eventState")
    Event toEvent(NewEventDto newEventDto,
                  Category category,
                  EventState eventState,
                  User initiator,
                  LocalDateTime localDateTime);

    List<EventShortDto> toEventShortDtoList(List<Event> events);

    List<EventFullDto> toEventFullDtoList(List<Event> events);
}
