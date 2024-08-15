package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Event;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(List<Long> users,
                                                                                   List<EventState> states,
                                                                                   List<Long> categories,
                                                                                   LocalDateTime start,
                                                                                   LocalDateTime end,
                                                                                   Pageable pageable);

    Page<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsBefore(List<Long> users,
                                                                                    List<EventState> states,
                                                                                    List<Long> categories, LocalDateTime end,
                                                                                    Pageable pageable);

    Page<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfter(List<Long> users, List<EventState> states,
                                                                                   List<Long> categories, LocalDateTime start,
                                                                                   Pageable pageable);

    Page<Event> findAllByInitiatorIdInAndStateInAndCategoryIdIn(List<Long> users, List<EventState> states,
                                                                List<Long> categories, Pageable pageable);
    Page<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByIdIn(List<Long> eventIds);

    Boolean existsByCategoryId(Long catId);

    Optional<Event> findByIdAndPublishedOnIsNotNull(Long id);

    @Query("SELECT e FROM Event e " +
            "WHERE (:text is null or ((lower(e.annotation) like lower(concat('%',:text,'%'))) or (lower(e.description) like lower(concat('%',:text,'%'))))) " +
            "and e.category.id in (:categories) " +
            "and (cast(:paid as boolean) is null or e.paid = :paid) " +
            "and e.eventDate > :start " +
            "and (cast(:end as timestamp) is null or e.eventDate < :end) " +
            "and e.state = 'PUBLISHED' " +
            "and (cast(:onlyAvailable as boolean) = false or e.participantLimit = 0 " +
            "or e.participantLimit > (SELECT COUNT(r) FROM Request r WHERE r.event.id = e.id AND r.status = 'CONFIRMED'))")
    Page<Event> search(@Param("text") String text,
                       @Param("categories") List<Long> categories,
                       @Param("paid") Boolean paid,
                       @Param("start") LocalDateTime rangeStart,
                       @Param("end") LocalDateTime rangeEnd,
                       @Param("onlyAvailable") Boolean onlyAvailable,
                       Pageable pageable);

    @Query("SELECT COUNT(r.id) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);
}
