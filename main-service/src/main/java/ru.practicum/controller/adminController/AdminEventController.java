package ru.practicum.controller.adminController;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.enums.EventState;
import ru.practicum.service.event.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.updateByAdmin(eventId, updateEventAdminRequest);

    }

    @GetMapping
    public List<EventFullDto> get(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<EventState> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) LocalDateTime rangeStart,
                                        @RequestParam(required = false) LocalDateTime rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                        @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return eventService.getWithParamsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}
