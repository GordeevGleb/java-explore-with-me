package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Request;
import ru.practicum.enums.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

   List<Request> findAllByEventAndIdIn(Long eventId, List<Long> requestIds);

    Boolean existsByRequesterAndEvent(Long userId, Long eventId);

    List<Request> findAllByRequester(Long userId);

    List<Request> findAllByEvent(Long eventId);

    Long countByEventAndStatusIs(Long eventId, RequestStatus requestStatus);

}
