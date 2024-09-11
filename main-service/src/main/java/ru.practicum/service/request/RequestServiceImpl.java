package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.*;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("MAIN SERVICE LOG: creating request");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new UserRequestAlreadyExistException("Request already exists.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new WrongUserRequestException("Event initiator can't send participation request");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventStatusException("Event was not published");
        }
        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new LimitException("Request limit has been reached");
        }
        Request actual = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();
        if (event.getRequestModeration().equals(Boolean.FALSE) || event.getParticipantLimit() == 0) {
            event.setConfirmedRequestCount(Optional.ofNullable(
                    event.getConfirmedRequestCount()).isEmpty() ? 1L : event.getConfirmedRequestCount() + 1);
            actual.setStatus(RequestStatus.CONFIRMED);
        }
        requestRepository.save(actual);
        ParticipationRequestDto participationRequestDto = requestMapper.toParticipationRequestDto(actual);
        log.info("MAIN SERVICE LOG: request created");
        return participationRequestDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("MAIN SERVICE LOG: getting user's event request list");
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Event event = eventRepository.findById(eventId).get();
        if (!event.getInitiator().getId().equals(userId)) {
            throw new WrongRequestException("User must be event initiator");
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        log.info("MAIN SERVICE LOG: user's event request list formed");
        return requestMapper.toParticipationRequestDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequests(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest
                                                                     eventRequestStatusUpdateRequest) {
        log.info("MAIN SERVICE LOG: updating requests");
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Event event = eventRepository.findById(eventId).get();
        if (Optional.ofNullable(eventRequestStatusUpdateRequest.getRequestIds()).isEmpty()) {
            throw new RequestStatusException("Field request id's shall not be blank");
        }
        List<Request> requests = requestRepository
                .findAllByEventIdAndIdIn(eventId, eventRequestStatusUpdateRequest.getRequestIds());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (event.getParticipantLimit() == 0 || event.getRequestModeration().equals(Boolean.FALSE)) {
            return result;
        }
        if (requests.stream().anyMatch(request -> !request.getStatus().equals(RequestStatus.PENDING))) {
            throw new RequestStatusException("Request must have status PENDING");
        }
        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventIdAndStatusIs(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new WrongRequestException("The participant limit has been reached");
        }
        if (Optional.ofNullable(event.getConfirmedRequestCount()).isEmpty()) {
            event.setConfirmedRequestCount(0L);
        }
        if (RequestStatus.CONFIRMED.equals(eventRequestStatusUpdateRequest.getStatus())) {
            requests.stream().forEach(request -> {
                 if (event.getConfirmedRequestCount() < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    event.setConfirmedRequestCount(event.getConfirmedRequestCount() + 1);
                } else if (event.getConfirmedRequestCount() >= event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.REJECTED);
                }
            });
        } else {
            requests.forEach(request -> request.setStatus(eventRequestStatusUpdateRequest.getStatus()));
        }
        requestRepository.saveAll(requests);
        eventRepository.save(event);
        requests.forEach(request -> {
            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                result.getConfirmedRequests().add(requestMapper.toParticipationRequestDto(request));
            }
            if (request.getStatus().equals(RequestStatus.REJECTED)) {
                result.getRejectedRequests().add(requestMapper.toParticipationRequestDto(request));
            }
        });
        log.info("MAIN SERVICE LOG: requests updated");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long id) {
        log.info("MAIN SERVICE LOG: getting user id " + id + " request list");
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " was not found");
        }
        List<Request> userRequests = requestRepository.findAllByRequesterId(id);
        log.info("MAIN SERVICE LOG: user request list formed");
        return requestMapper.toParticipationRequestDtoList(userRequests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("MAIN SERVICE LOG: user id " + userId + " cancel request id " + requestId);
        if (!existById(requestId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }
        Request request = requestRepository.findById(requestId).get();
        request.setStatus(RequestStatus.CANCELED);
        log.info("MAIN SERVICE LOG: request cancelled");
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private Boolean existById(Long id) {
        return requestRepository.existsById(id);
    }
}
