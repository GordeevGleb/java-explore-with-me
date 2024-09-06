package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.entity.Event;
import ru.practicum.entity.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {

    List<Rating> findAllByEventId(Long eventId);

    void removeAllByUserId(Long userId);

    Boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<Rating> findAllByUserId(Long userId);
}
