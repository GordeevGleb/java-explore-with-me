package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Event;
import ru.practicum.entity.Request;
import ru.practicum.enums.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

   List<Request> findAllByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventId(Long eventId);

    Long countByEventIdAndStatusIs(Long eventId, RequestStatus requestStatus);

    List<Request> findAllByEventInAndStatusIs(List<Event> events, RequestStatus requestStatus);

}
