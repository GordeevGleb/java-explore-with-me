package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitRequestDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.dto.event.*;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.User;
import ru.practicum.enums.AdminStateAction;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortFormat;
import ru.practicum.enums.UserStateAction;
import ru.practicum.exception.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;

    private final EventMapper eventMapper;

    private final UserRepository userRepository;

    private final StatsClient statsClient;


    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        log.info("MAIN SERVICE LOG: user id " + userId + " creating event");
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category was not found"));
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DateTimeException("Field: eventDate. Error: должно содержать дату, которая еще не наступила." +
                    " Value:" + eventDate);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventMapper.toEvent(newEventDto, category, EventState.PENDING, user, LocalDateTime.now());
        if (Optional.ofNullable(event.getPaid()).isEmpty()) {
            event.setPaid(Boolean.FALSE);
        }
        if (Optional.ofNullable(event.getParticipantLimit()).isEmpty()) {
            event.setParticipantLimit(0L);
        }
        log.info("MAIN SERVICE LOG: event created");
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get user's events");
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> actual = eventRepository.findAllByInitiatorId(userId, pageRequest).toList();
        log.info("MAIN SERVICE LOG: user id " + userId + " event list formed");
        return eventMapper.toEventShortDtoList(actual);
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (Optional.ofNullable(updateEventAdminRequest).isEmpty()) {
            return eventMapper.toEventFullDto(event);
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new EventDateTimeException("Cannot publish the event because of date time restriction");
        }
        if (updateEventAdminRequest.getEventDate() != null &&
                updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new EventDateTimeException("Cannot publish the event because of date time restriction");
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(
                            () -> new NotFoundException(
                                    "Category with id=" + updateEventAdminRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(updateEventAdminRequest.getLocation());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            AdminStateAction aState = updateEventAdminRequest.getStateAction();
            if (aState.equals(AdminStateAction.PUBLISH_EVENT)) {
                if (event.getPublishedOn() != null || event.getState().equals(EventState.CANCELED)) {
                    throw new EventStatusException("Cannot publish the event because it's not in " +
                            "the right state: " + event.getState());
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (aState.equals(AdminStateAction.REJECT_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new EventStatusException("Cannot publish the event because it's not in " +
                            "the right state: " + event.getState());
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (Optional.ofNullable(updateEventAdminRequest.getEventDate()).isPresent()) {
            LocalDateTime eventDateTime = updateEventAdminRequest.getEventDate();
            if (Optional.ofNullable(event.getPublishedOn()).isPresent()) {
                if (eventDateTime.isBefore(LocalDateTime.now())
                        || eventDateTime.isBefore(event.getPublishedOn().plusHours(1))) {
                    throw new DateTimeException("The start date of the event to be modified" +
                            " is less than one hour from the publication date.");
                }
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("MAIN SERICE LOG: user id " + userId + " updating event id " + eventId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (Optional.ofNullable(updateEventUserRequest).isEmpty()) {
            return eventMapper.toEventFullDto(event);
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new EventStatusException("Only pending or canceled events can be changed");
        }
        if (Optional.ofNullable(updateEventUserRequest.getEventDate()).isPresent()) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new EventDateTimeException("Event date exception");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Long categoryId = updateEventUserRequest.getCategory();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(updateEventUserRequest.getLocation());
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null && updateEventUserRequest.getTitle().length() >= 3) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        log.info("MAIN SERVICE LOG: event updated by user");
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByIdUser(Long userId, Long eventId) {
        log.info("MAIN SERVICE LOG: getting event id " + eventId + " by user id " + userId);
        Event actual = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        log.info("MAIN SERVICE LOG: event found");
        return eventMapper.toEventFullDto(actual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getWithParamsAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size.intValue());

        List<Event> events;

        if (users == null) {
            users = userRepository.findAllId();
        }
        if (categories == null) {
            categories = categoryRepository.findAllId();
        }
        if (states == null) {
            states = List.of(EventState.PUBLISHED, EventState.CANCELED, EventState.PENDING);
        }

        if (rangeStart != null && rangeEnd != null) {
            events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(users, states, categories, rangeStart, rangeEnd, pageable)
                    .getContent();
        } else if (rangeStart != null) {
            events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfter(users, states, categories, rangeStart, pageable)
                    .getContent();
        } else if (rangeEnd != null) {
            events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsBefore(users, states, categories, rangeEnd, pageable)
                    .getContent();
        } else {
            events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdIn(users, states, categories, pageable).getContent();
        }

        List<EventFullDto> result = eventMapper.toEventFullDtoList(events);

        result = result.stream()
                .peek(event -> event.setConfirmedRequests(getConfirmedRequestsCount(event.getId())))
                .collect(Collectors.toList());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getWithParams(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, Boolean onlyAvailable, SortFormat sort,
                                             Integer from, Integer size, HttpServletRequest request) {
        int page = (from / size);
        Pageable pageable = PageRequest.of(page, size);

        if (rangeEnd != null && rangeStart != null && rangeEnd.isBefore(rangeStart)) {
            throw new WrongInputException("Incorrect dateTime");
        }

        if (categories == null) {
            categories = categoryRepository.findAllId();
        }

        if (rangeEnd == null && rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        List<Event> events = eventRepository.search(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable)
                .getContent();

        Map<Long, Long> views = getViews(events);

        List<EventShortDto> result = eventMapper.toEventShortDtoList(events);
        result = result.stream()
                .peek(event -> event.setViews(views.getOrDefault(event.getId(), 0L)))
                .peek(event -> event.setConfirmedRequests(getConfirmedRequestsCount(event.getId())))
                .collect(Collectors.toList());

        sendHit(request);

        if (sort != null && sort == SortFormat.EVENT_DATE) {
            result = result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .collect(Collectors.toList());
        } else if (sort != null && sort == SortFormat.VIEWS) {
            result = result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByIdPublic(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", id)));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d was not found", id));
        }

        Map<Long, Long> views = getViews(List.of(event));

        EventFullDto result = eventMapper.toEventFullDto(event);
        result.setConfirmedRequests(getConfirmedRequestsCount(result.getId()));
        result.setViews(views.getOrDefault(event.getId(), 0L));

        sendHit(request);

        return result;
    }

    private Map<Long, Long> getViews(List<Event> events) {
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException(""));

        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<ViewStatsResponseDto> views = statsClient.getStats(start, LocalDateTime.now(), uris, true);

        Map<Long, Long> eventViews = new HashMap<>();

        for (ViewStatsResponseDto view : views) {
            if (view.getUri().equals("/events")) {
                continue;
            }
            Long eventId = Long.parseLong(view.getUri().substring("/events".length() + 1));
            eventViews.put(eventId, view.getHits());
        }

        return eventViews;
    }

    private Long getConfirmedRequestsCount(Long eventId) {
        return eventRepository.countConfirmedRequestsByEventId(eventId);
    }

    private void sendHit(HttpServletRequest request) {
        statsClient.addStats(EndpointHitRequestDto.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
