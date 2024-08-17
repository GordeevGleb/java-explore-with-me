package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Event;
import ru.practicum.enums.EventState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

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

    @Query("SELECT COUNT(r.id) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);
}
