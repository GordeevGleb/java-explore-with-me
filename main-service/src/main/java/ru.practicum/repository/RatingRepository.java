package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.entity.Rating;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {

    List<Rating> findAllByEventId(Long eventId);

    void removeAllByUserId(Long userId);

    Boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<Rating> findAllByUserId(Long userId);

    Boolean existsByUserId(Long userId);
}
