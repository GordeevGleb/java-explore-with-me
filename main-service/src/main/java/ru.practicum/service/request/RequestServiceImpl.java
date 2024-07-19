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

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("MAIN SERVICE LOG: creating request");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (requestRepository.existsByRequesterAndEvent(userId, eventId)) {
            throw new UserRequestAlreadyExistException("Request already exists.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new IntegrityConflictException("Event initiator can't send participation request");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new IntegrityConflictException("Event was not published");
        }
        if (requestRepository.findAllByEvent(eventId).size() >= event.getParticipantLimit()) {
            throw new LimitException("Request limit has been reached");
        }
        Request actual = Request.builder()
                .created(LocalDateTime.now())
                .event(eventId)
                .requester(userId)
                .status(RequestStatus.PENDING)
                .build();
        if (event.getRequestModeration().equals(Boolean.FALSE) || event.getParticipantLimit() == 0) {
            actual.setStatus(RequestStatus.CONFIRMED);
        }
        ParticipationRequestDto participationRequestDto = requestMapper.toParticipationRequestDto(actual);
        log.info("MAIN SERVICE LOG: request created");
        return participationRequestDto;
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("MAIN SERVICE LOG: getting user's event request list");
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new WrongRequestException("User must be event initiator");
        }
        List<Request> requests = requestRepository.findAllByEvent(eventId);
        log.info("MAIN SERVICE LOG: user's event request list formed");
        return requestMapper.toParticipationRequestDtoList(requests);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequests(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest
                                                                     eventRequestStatusUpdateRequest) {
        log.info("MAIN SERVICE LOG: updating requests");
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        List<Request> requests = requestRepository
                .findAllByEventAndIdIn(eventId, eventRequestStatusUpdateRequest.getRequestIds());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (event.getParticipantLimit() == 0 || event.getRequestModeration().equals(Boolean.FALSE)) {
            return result;
        }
        if (requests.stream().anyMatch(request -> !request.getStatus().equals(RequestStatus.PENDING))) {
            throw new RequestStatusException("Request must have status PENDING");
        }
        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventAndStatusIs(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new IntegrityConflictException("The participant limit has been reached");
        }
        if (RequestStatus.CONFIRMED.equals(eventRequestStatusUpdateRequest.getStatus())) {
            requests.stream().forEach(request -> {
                if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                } else if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.REJECTED);
                }
            });
        } else {
            requests.stream().forEach(request -> request.setStatus(eventRequestStatusUpdateRequest.getStatus()));
        }
        requestRepository.saveAll(requests);
        eventRepository.save(event);
        List<ParticipationRequestDto> participationRequestDtos = requestMapper.toParticipationRequestDtoList(requests);
        participationRequestDtos.stream()
                        .forEach(participationRequestDto -> {
                            if (participationRequestDto.getStatus().equals(RequestStatus.CONFIRMED)) {
                                result.getConfirmedRequests().add(participationRequestDto);
                            } else if (participationRequestDto.getStatus().equals(RequestStatus.REJECTED)) {
                                result.getRejectedRequests().add(participationRequestDto);
                            }
                        });
        log.info("MAIN SERVICE LOG: requests updated");
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long id) {
        log.info("MAIN SERVICE LOG: getting user id " + id + " request list");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " was not found"));
        List<Request> userRequests = requestRepository.findAllByRequester(id);
        log.info("MAIN SERVICE LOG: user request list formed");
        return requestMapper.toParticipationRequestDtoList(userRequests);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("MAIN SERVICE LOG: user id " + userId + " cancel request id " + requestId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        request.setStatus(RequestStatus.PENDING);
        log.info("MAIN SERVICE LOG: request cancelled");
        return requestMapper.toParticipationRequestDto(request);
    }
}
