package ru.practicum.controller.publicController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.enums.SortFormat;
import ru.practicum.service.event.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getWithParams(@RequestParam(name = "text", required = false) String text,
                                   @RequestParam(name = "categories", required = false) List<Long> categories,
                                   @RequestParam(name = "paid", required = false) Boolean paid,
                                   @RequestParam(name = "rangeStart", required = false) LocalDateTime rangeStart,
                                   @RequestParam(name = "rangeEnd", required = false) LocalDateTime rangeEnd,
                                   @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                   @RequestParam(name = "sort", required = false) SortFormat sortFormat,
                                   @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
                                   @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                   HttpServletRequest request) {
        return eventService.getWithParams(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sortFormat, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getByIdPublic(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getByIdPublic(id, request);
    }
}
