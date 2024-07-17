package ru.practicum.service.event;

import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.*;
import ru.practicum.entity.Event;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortFormat;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getById(Long userId, Long eventId);

    List<EventFullDto> getWithParamsAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                                  String rangeStart, String rangeEnd, Integer from, Integer size);

    List<EventShortDto> getWithParams(String text, List<Long> categories, Boolean paid, String rangeStart,
                                       String rangeEnd, Boolean onlyAvailable, SortFormat sort, Integer from,
                                       Integer size, HttpServletRequest request);

    EventFullDto getById(Long id, HttpServletRequest request);

    void setView(List<Event> events);
}
