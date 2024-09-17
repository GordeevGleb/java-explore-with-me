package ru.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.rating.EventFullRatingDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.enums.*;
import ru.practicum.exception.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.service.rating.RatingService;

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

    private final RequestRepository requestRepository;

    private final StatsClient statsClient;

    private final RatingService ratingService;

    private final EntityManager entityManager;


    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        log.info("MAIN SERVICE LOG: user id " + userId + " creating event");
        if (!categoryRepository.existsById(newEventDto.getCategory())) {
            throw new NotFoundException("Category was not found");
        }
        Category category = categoryRepository.findById(newEventDto.getCategory()).get();
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DateTimeException("Field: eventDate. Error: должно содержать дату, которая еще не наступила." +
                    " Value:" + eventDate);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventMapper.toEvent(newEventDto, category, EventState.PENDING, user,
                LocalDateTime.now(), new HashSet<>());
        if (Optional.ofNullable(event.getPaid()).isEmpty()) {
            event.setPaid(Boolean.FALSE);
        }
        if (Optional.ofNullable(event.getParticipantLimit()).isEmpty()) {
            event.setParticipantLimit(0L);
        }
        log.info("MAIN SERVICE LOG: event created");
        return eventMapper.toEventFullDto(eventRepository.save(event), new EventFullRatingDto());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get user's events");
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("user id " + userId + " not found");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> actual = eventRepository.findAllByInitiatorId(userId, pageRequest).toList();
        log.info("MAIN SERVICE LOG: user id " + userId + " event list formed");
        List<EventShortDto> resultList = actual.stream()
                .map(event -> eventMapper.toEventShortDto(event, ratingService.calculateEventShortRating(event)))
                .collect(Collectors.toList());
        return resultList;
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        if (!validateById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Event event = eventRepository.findById(eventId).get();
        if (Optional.ofNullable(updateEventAdminRequest).isEmpty()) {
            return eventMapper.toEventFullDto(event, ratingService.calculateEventRating(event));
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

        return eventMapper.toEventFullDto(eventRepository.save(event), ratingService.calculateEventRating(event));
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("MAIN SERICE LOG: user id " + userId + " updating event id " + eventId);
        if (!validateByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException("Event with id=" + eventId + " and initiator id " + userId + " was not found");
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).get();
        if (Optional.ofNullable(updateEventUserRequest).isEmpty()) {
            return eventMapper.toEventFullDto(event, ratingService.calculateEventRating(event));
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
        return eventMapper.toEventFullDto(eventRepository.save(event), ratingService.calculateEventRating(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByIdUser(Long userId, Long eventId) {
        log.info("MAIN SERVICE LOG: getting event id " + eventId + " by user id " + userId);
        if (!validateByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException("Event with id=" + eventId + " and initiator id " + userId + " was not found");
        }
        Event actual = eventRepository.findByIdAndInitiatorId(eventId, userId).get();
        log.info("MAIN SERVICE LOG: event found");
        return eventMapper.toEventFullDto(actual, ratingService.calculateEventRating(actual));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getWithParamsAdmin(List<Long> usersIds, List<EventState> states, List<Long> categoriesIds,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Integer from, Integer size) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (categoriesIds != null && categoriesIds.size() > 0) {
            List<Category> categories = categoryRepository.findAllByIdIn(categoriesIds);
            Predicate containCategories = root.get("category").in(categories);
            criteria = builder.and(criteria, containCategories);
        }

        if (usersIds != null && usersIds.size() > 0) {
            List<User> users = userRepository.findAllByIdIn(usersIds);
            Predicate containUsers = root.get("initiator").in(users);
            criteria = builder.and(criteria, containUsers);
        }

        if (states != null) {
            Predicate containStates = root.get("state").in(states);
            criteria = builder.and(criteria, containStates);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeStart);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeEnd);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria);
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.size() == 0) {
            return new ArrayList<>();
        }

        Map<Long, Long> views = getViews(events);

        Map<Long, Long> requests = getConfirmedRequests(events);

        events = events.stream()
                .peek(event -> event.setConfirmedRequestCount(requests.getOrDefault(event.getId(), 0L)))
                .peek(event -> event.setViewCount(views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
        List<EventFullDto> resultList = events.stream()
                .map(event -> eventMapper.toEventFullDto(event, ratingService.calculateEventRating(event)))
                .collect(Collectors.toList());
        return resultList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getWithParams(String text, List<Long> categoriesIds, Boolean paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, Boolean onlyAvailable, SortFormat sort,
                                             Integer from, Integer size, HttpServletRequest request) {
        log.info("MAIN SERVICE LOG: getting events with params public");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateTimeException("Date time exception");
        }

        if (Optional.ofNullable(rangeStart).isEmpty()) {
            rangeStart = LocalDateTime.now();
        }

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (text != null) {
            Predicate annotationContain = builder.like(builder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%");
            Predicate descriptionContain = builder.like(builder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%");
            Predicate containText = builder.or(annotationContain, descriptionContain);

            criteria = builder.and(criteria, containText);
        }

        if (categoriesIds != null && categoriesIds.size() > 0) {
            List<Category> categories = categoryRepository.findAllByIdIn(categoriesIds);
            Predicate containStates = root.get("category").in(categories);
            criteria = builder.and(criteria, containStates);
        }

        if (paid != null) {
            Predicate isPaid;
            if (paid) {
                isPaid = builder.isTrue(root.get("paid"));
            } else {
                isPaid = builder.isFalse(root.get("paid"));
            }
            criteria = builder.and(criteria, isPaid);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeStart);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), rangeEnd);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria).orderBy(builder.asc(root.get("eventDate")));
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (onlyAvailable) {
            events = events.stream()
                    .filter((event -> event.getConfirmedRequestCount() < (long) event.getParticipantLimit()))
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            if (sort.equals(SortFormat.EVENT_DATE)) {
                events = events.stream().sorted(Comparator.comparing(Event::getEventDate)).collect(Collectors.toList());
            } else {
                events = events.stream().sorted(Comparator.comparing(Event::getViewCount)).collect(Collectors.toList());
            }
        }

        if (events.size() == 0) {
            return new ArrayList<>();
        }

        sendHit(request);

        Map<Long, Long> views = getViews(events);

        Map<Long, Long> requests = getConfirmedRequests(events);

        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toEventShortDto(event, ratingService.calculateEventShortRating(event)))
                .collect(Collectors.toList());

        result = result.stream()
                .peek(event -> event.setViews(views.getOrDefault(event.getId(), 0L)))
                .peek(event -> event.setConfirmedRequests(requests.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByIdPublic(Long id, HttpServletRequest request) {
        if (!validateById(id)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", id));
        }
        Event event = eventRepository.findById(id).get();
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d was not found", id));
        }

        Map<Long, Long> views = getViews(List.of(event));

        Map<Long, Long> requests = getConfirmedRequests(List.of(event));

        EventFullDto result = eventMapper.toEventFullDto(event, ratingService.calculateEventRating(event));
        result.setConfirmedRequests(requests.getOrDefault(event.getId(), 0L));
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

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventInAndStatusIs(events, RequestStatus.CONFIRMED);
        Map<Long, Long> result = new HashMap<>();
        for (Event event : events) {
            for (Request request : requests) {
                if (request.getEvent().getId().equals(event.getId())) {
                    result.put(event.getId(), request.getId());
                }
            }
        }
        log.info("MAIN SERVICE LOG: result events " + result.keySet() + " result requests " + result.values());
        return result;
    }

    private void sendHit(HttpServletRequest request) {
        statsClient.addStats(EndpointHitDto.builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Boolean validateById(Long id) {
        return eventRepository.existsById(id);
    }

    private Boolean validateByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.existsByIdAndInitiatorId(eventId, userId);
    }
}
