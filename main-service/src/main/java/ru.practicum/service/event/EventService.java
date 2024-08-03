package ru.practicum.service.event;

import ru.practicum.dto.event.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortFormat;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getByIdUser(Long userId, Long eventId);

    List<EventFullDto> getWithParamsAdmin(List<Long> users, List<EventState> states, List<Long> categoriesId,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    List<EventShortDto> getWithParams(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd, Boolean onlyAvailable, SortFormat sort, Integer from,
                                       Integer size, HttpServletRequest request);

    EventFullDto getByIdPublic(Long id, HttpServletRequest request);
}
