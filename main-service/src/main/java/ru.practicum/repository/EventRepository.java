package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Event;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByIdIn(List<Long> eventIds);

    Boolean existsByCategoryId(Long catId);

//    @Query("select e from Event as e join Rating as r on r.event.id = e.id having ((avg(r.locationRate) > :locationRate) and " +
//            "(avg(r.organizationRate) > :organizationRate) and (avg(r.contentRate) > :contentRate))")
//    List<Event> getByRatingParams(Float locationRate, Float organizationRate, Float contentRate);

    @Query("SELECT e FROM Event e JOIN e.ratingList r " +
            "GROUP BY e " +
            "HAVING AVG(r.locationRate) > :locationRate AND " +
            "AVG(r.organizationRate) > :organizationRate AND " +
            "AVG(r.contentRate) > :contentRate")
    List<Event> getByRatingParams(Float locationRate, Float organizationRate, Float contentRate);
}
