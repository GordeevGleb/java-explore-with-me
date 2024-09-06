package ru.practicum.service.rating;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.rating.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Rating;
import ru.practicum.entity.User;
import ru.practicum.exception.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RatingRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    private final RatingRepository ratingRepository;

    private final RatingMapper ratingMapper;

    private final EntityManager entityManager;


    @Override
    public OutRatingDto create(Long userId, Long eventId, IncRatingDto incRatingDto) {
        log.info("FEATURE SERVICE LOG: user id " + userId + " rate event id " + eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user id " + userId + " not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->  new NotFoundException("event id " + eventId + " not found"));
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new WrongRequestException("initiator can't rate event");
        }
        if (ratingRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new IntegrityConflictException("can't rate again");
        }

        Rating actual = ratingMapper.toRating(user, event, incRatingDto, LocalDateTime.now());
        UserDto userDto = userMapper.toUserDto(user);
        EventShortRatingDto eventShortRatingDto = calculateEventShortRating(event);
        EventShortDto eventShortDto = eventMapper.toEventShortDto(event, eventShortRatingDto);
        OutRatingDto outRatingDto = ratingMapper.toOutRatingDto(ratingRepository.save(actual), userDto, eventShortDto);
        log.info("FEATURE SERVICE LOG: rating saved");
        return outRatingDto;
    }

    @Override
    public OutRatingDto update(Long userId, Long ratingId, IncRatingDto incRatingDto) {
        log.info("FEATURE SERVICE LOG: private updating info about rating id " + ratingId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id " + userId + " do not exist"));
        Rating actual = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating id " + ratingId + " do not exist"));
        if (!user.getId().equals(actual.getUser().getId())) {
            throw new IntegrityConflictException("another user can't update rating");
        }
        if (actual.getUpdateTime().plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw new RatingDateTimeUpdateException("Обновлять информацию о рейтинге можно только раз в сутки(выставлено меньшее значение для тестов)");
        }
        if (Optional.ofNullable(incRatingDto.getIsLiked()).isPresent()) {
            actual.setIsLiked(incRatingDto.getIsLiked());
        }
        if (Optional.ofNullable(incRatingDto.getContentRate()).isPresent()) {
            actual.setContentRate(incRatingDto.getContentRate());
        }
        if (Optional.ofNullable(incRatingDto.getLocationRate()).isPresent()) {
            actual.setLocationRate(incRatingDto.getLocationRate());
        }
        if (Optional.ofNullable(incRatingDto.getOrganizationRate()).isPresent()) {
            actual.setOrganizationRate(incRatingDto.getOrganizationRate());
        }
        actual.setUpdateTime(LocalDateTime.now());
        UserDto userDto = userMapper.toUserDto(user);
        Event event = actual.getEvent();
        EventShortRatingDto eventShortRatingDto = calculateEventShortRating(event);
        EventShortDto eventShortDto = eventMapper.toEventShortDto(event, eventShortRatingDto);
        OutRatingDto outRatingDto = ratingMapper.toOutRatingDto(ratingRepository.save(actual), userDto, eventShortDto);
        log.info("FEATURE SERVICE LOG: rating id " + ratingId + " updated");
        return outRatingDto;
    }

    @Override
    public OutRatingDto updateByAdmin(Long ratingId, IncRatingDto incRatingDto) {
        log.info("FEATURE SERVICE LOG: admin updating info about rating id " + ratingId);
        Rating actual = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("rating id " + ratingId + " not found"));
        if (Optional.ofNullable(incRatingDto.getIsLiked()).isPresent()) {
            actual.setIsLiked(incRatingDto.getIsLiked());
        }
        if (Optional.ofNullable(incRatingDto.getContentRate()).isPresent()) {
            actual.setContentRate(incRatingDto.getContentRate());
        }
        if (Optional.ofNullable(incRatingDto.getLocationRate()).isPresent()) {
            actual.setLocationRate(incRatingDto.getLocationRate());
        }
        if (Optional.ofNullable(incRatingDto.getOrganizationRate()).isPresent()) {
            actual.setOrganizationRate(incRatingDto.getOrganizationRate());
        }
        User user = actual.getUser();
        UserDto userDto = userMapper.toUserDto(user);
        Event event = actual.getEvent();
        EventShortRatingDto eventShortRatingDto = calculateEventShortRating(event);
        EventShortDto eventShortDto = eventMapper.toEventShortDto(event, eventShortRatingDto);
        OutRatingDto outRatingDto = ratingMapper.toOutRatingDto(ratingRepository.save(actual), userDto, eventShortDto);
        log.info("FEATURE SERVICE LOG: rating id " + ratingId + " updated");
        return outRatingDto;
    }

    @Override
    public void delete(Long ratingId) {
        log.info("FEATURE SERVICE LOG: delete rating id " + ratingId);
        if (ratingRepository.existsById(ratingId)) {
            ratingRepository.deleteById(ratingId);
        }
        log.info("FEATURE SERVICE LOG: rating removed");
    }

    @Override
    public void deleteByUserId(Long userId) {
        log.info("FEATURE SERVICE LOG: removing all user id " + userId + " ratings");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user id " + userId + " not found"));
        ratingRepository.removeAllByUserId(userId);
        log.info("FEATURE SERVICE LOG: all users rating was removed");
    }

    @Override
    @Transactional(readOnly = true)
    public OutRatingDto getById(Long ratingId) {
        log.info("FEATURE SERVICE LOG: getting rating id {}", ratingId);
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("rating id " + ratingId + " not found"));
        UserDto userDto = userMapper.toUserDto(rating.getUser());
        EventShortDto eventShortDto = eventMapper
                .toEventShortDto(rating.getEvent(), calculateEventShortRating(rating.getEvent()));
        log.info("FEATURE SERVICE LOG: rating id {} found", ratingId);
        return ratingMapper.toOutRatingDto(rating, userDto, eventShortDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutRatingDto> getAllUsersRatings(Long userId, Boolean likesOrDislikesOnly,
                                                 Integer from, Integer size) {
        log.info("FEATURE SERVICE LOG: get all user");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);

        Root<Rating> root = query.from(Rating.class);
        Predicate criteria = builder.conjunction();

        if (likesOrDislikesOnly != null) {
            Predicate isLiked;
            if (likesOrDislikesOnly) {
                isLiked = builder.isTrue(root.get("isLiked"));
            } else {
                isLiked = builder.isFalse(root.get("isLiked"));
            }
            criteria = builder.and(criteria, isLiked);
        }

        query.select(root).where(criteria);
        List<Rating> userRatings = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        log.info("FEATURE SERVICE LOG: user rating list formed; size {}", userRatings.size());
        return ratingMapper.toOutRatingDtoList(userRatings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutRatingDto> getAll(Boolean likesOrDislikesOnly,
                                     Integer from,
                                     Integer size) {
        log.info("FEATURE SERVICE LOG: get all rating list with params");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);

        Root<Rating> root = query.from(Rating.class);
        Predicate criteria = builder.conjunction();

        if (likesOrDislikesOnly != null) {
            Predicate isLiked;
            if (likesOrDislikesOnly) {
                isLiked = builder.isTrue(root.get("isLiked"));
            } else {
                isLiked = builder.isFalse(root.get("isLiked"));
            }
            criteria = builder.and(criteria, isLiked);
        }

        query.select(root).where(criteria);
        List<Rating> ratings = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        log.info("FEATURE SERVICE LOG: rating list formed");
        return ratingMapper.toOutRatingDtoList(ratings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutRatingShortDto> getByEventId(Long eventId) {
        log.info("FEATURE SERVICE LOG: getting rating list by event id {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("event id " + eventId + " not found");
        }
        List<Rating> ratings = ratingRepository.findAllByEventId(eventId);
        List<OutRatingShortDto> ratingShortDtos = ratings.stream()
                .map(rating -> ratingMapper.toOutRatingShortDto(rating, userMapper.toUserDto(rating.getUser())))
                .toList();
        log.info("FEATURE SERVICE LOG: rating list by event id {} found; size {}", eventId, ratingShortDtos.size());
        return ratingShortDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutRatingDto> getByUserId(Long userId) {
        log.info("FEATURE SERVICE LOG: getting rating list by user id {}", userId);
        List<Rating> ratings = ratingRepository.findAllByUserId(userId);
        List<OutRatingDto> outRatingDtos = ratingMapper.toOutRatingDtoList(ratings);
        log.info("FEATURE SERVICE LOG: rating list formed; size {}", outRatingDtos.size());
        return outRatingDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getByRatingParams(Float locationRate,
                                                 Float organizationRate,
                                                 Float contentRate) {
        log.info("FEATURE SERVICE LOG: getting event list by rating params");
        List<Event> events = eventRepository.getByRatingParams(locationRate, organizationRate, contentRate);
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> eventMapper.toEventShortDto(event, calculateEventShortRating(event))).toList();
        log.info("FEATURE SERVICE LOG: rating param list formed; size {}", eventShortDtos.size());
        return eventShortDtos;
    }

    public EventFullRatingDto calculateEventRating(Event event) {
            List<Rating> eventRatings = event.getRatingList();
            int likesCount = 0;
            int dislikesCount = 0;
            float contentSum = 0.0f;
            float locationSum = 0.0f;
            float organizationSum = 0.0f;
            int contentCount = 0;
            int locationCount = 0;
            int organizationCount = 0;
            for (Rating rating : eventRatings) {
                if (Optional.ofNullable(rating.getContentRate()).isPresent()) {
                    contentSum += rating.getContentRate();
                    contentCount++;
                }
                if (Optional.ofNullable(rating.getOrganizationRate()).isPresent()) {
                    organizationSum += rating.getOrganizationRate();
                    organizationCount++;
                }
                if (Optional.ofNullable(rating.getLocationRate()).isPresent()) {
                    locationSum += rating.getLocationRate();
                    locationCount++;
                }
                if (Optional.ofNullable(rating.getIsLiked()).isPresent()) {
                    if (rating.getIsLiked().equals(Boolean.TRUE)) {
                        likesCount++;
                    } else {
                        dislikesCount++;
                    }
                }
            }
            int percentRate = (likesCount == 0 && dislikesCount == 0) ? 0 : (likesCount * 100) / (dislikesCount + likesCount);
            EventFullRatingDto eventFullRatingDto = EventFullRatingDto.builder()
                    .contentRate(contentSum / contentCount)
                    .locationRate(locationSum / locationCount)
                    .organizationRate(organizationSum / organizationCount)
                    .percentRating(percentRate)
                    .build();
        return eventFullRatingDto;
    }

    public EventShortRatingDto calculateEventShortRating(Event event) {
        List<Rating> eventRatings = event.getRatingList();
        int likesCount = 0;
        int dislikesCount = 0;
        for (Rating rating : eventRatings) {
            if (Optional.ofNullable(rating.getIsLiked()).isPresent()) {
                if (rating.getIsLiked().equals(Boolean.TRUE)) {
                    likesCount++;
                } else {
                    dislikesCount++;
                }
            }
        }
        int percentRate = (likesCount == 0 && dislikesCount == 0) ? 0 : (likesCount * 100) / (dislikesCount + likesCount);
        return EventShortRatingDto.builder().percentRating(percentRate).build();
    }
}
