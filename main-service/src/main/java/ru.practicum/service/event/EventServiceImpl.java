package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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
import ru.practicum.service.stats.StatsService;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsService statsService;


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
        Event event = eventMapper.toEvent(newEventDto, category,EventState.PENDING, user, LocalDateTime.now());
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
    public List<EventFullDto> getWithParamsAdmin(List<Long> users, List<EventState> states, List<Long> categoriesId,
                                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get parametryzed event list: admin");
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Specification<Event> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (users != null && users.size() > 0) {
                predicates.add(builder.and(root.get("initiator").in(users)));
            }
            if (states != null) {
                predicates.add(builder.and(root.get("state").in(states)));
            }
            if (categoriesId != null && categoriesId.size() > 0) {
                predicates.add(builder.and(root.get("category").in(categoriesId)));
            }
            if (rangeStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeEnd));
            }
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        List<Event> events = eventRepository.findAll(specification, page).stream()
                .collect(Collectors.toList());
        events = events.stream().peek(event -> {
            if (Optional.ofNullable(event.getConfirmedRequestCount()).isEmpty()) {
                event.setConfirmedRequestCount(0L);
            }
        }).collect(Collectors.toList());
        events = setView(events);
        List<EventFullDto> eventFullDtos = events.stream()
                .map(event -> eventMapper.toEventFullDto(event)).collect(Collectors.toList());
        log.info("MAIN SERVICE LOG: event list formed");
        return eventFullDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getWithParams(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable, SortFormat sort,
                                               Integer from, Integer size, HttpServletRequest request) {
        log.info("MAIN SERVICE LOG: event list forming");
        PageRequest pageRequest;
        if (Optional.ofNullable(sort).isEmpty()) {
            pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        } else if (sort.equals(SortFormat.EVENT_DATE)) {
            pageRequest = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("eventDate"));
        } else {
            pageRequest = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("views"));
        }
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart) || rangeStart.equals(rangeEnd)) {
                throw new WrongInputException("wrong date time params exception");
            }
        }
        LocalDateTime start = (rangeStart == null && rangeEnd == null) ? LocalDateTime.now() : rangeStart;
        Specification<Event> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (text != null) {
                predicates.add(builder.or(
                        builder.like(
                                builder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                        builder.like(
                                builder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                ));
            }
            if (categories != null && categories.size() > 0) {
                predicates.add(builder.and(root.get("category").in(categories)));
            }
            if (paid != null) {
                if (paid.booleanValue()) {
                    predicates.add(builder.isTrue(root.get("paid")));
                } else {
                    predicates.add(builder.isFalse(root.get("paid")));
                }
            }
            if (start != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start));
            }
            if (rangeEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeEnd));
            }
            predicates.add(builder.equal(root.get("state").as(String.class), EventState.PUBLISHED.toString()));
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        List<Event> events = eventRepository.findAll(specification, pageRequest).stream()
                .collect(Collectors.toList());
        log.info("MAIN SERVICE LOG: event list formed");
        statsService.sendStat(events, request);
        events = setView(events);
        return eventMapper.toEventShortDtoList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByIdPublic(Long id, HttpServletRequest request) {
        log.info("MAIN SERVICE LOG: get event id " + id);
        Event event = eventRepository.findByIdAndPublishedOnIsNotNull(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
        log.info("request : " + request.getRequestURI());
        statsService.sendStat(event, request);
        event = setView(List.of(event)).get(0);
        log.info("MAIN SERVICE LOG: event id " + id + " found; views: " + event.getViewCount());
        return eventMapper.toEventFullDto(event);
    }

    public List<Event> setView(List<Event> events) {
        String start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("date time exception"))
                .format(dateFormatter);

        String endTime = LocalDateTime.now().format(dateFormatter);

        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<ViewStatsResponseDto> viewStatsResponseDtos = statsService.getStats(start, endTime, uris);
        Map<Long, Long> eventHits = new HashMap<>();

        for (ViewStatsResponseDto viewStatsResponseDto : viewStatsResponseDtos) {
            if (viewStatsResponseDto.getUri().equals("/events/")) {
                continue;
            }
            Long eventId = Long.parseLong(viewStatsResponseDto.getUri().substring("/events/".length() + 1));
            eventHits.put(eventId, viewStatsResponseDto.getHits());
        }
        events = events.stream()
                .peek(event ->
                        event.setViewCount(eventHits.getOrDefault(event.getId(), 1L))).collect(Collectors.toList());
        return events;
    }
}
