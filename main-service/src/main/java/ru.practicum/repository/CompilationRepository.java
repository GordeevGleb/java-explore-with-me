package ru.practicum.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    List<Compilation> findByPinned(PageRequest pageRequest, Boolean pinned);
    @Query("select c FROM Compilation c join fetch c.events")
    List<Compilation> findAllWithEvents(PageRequest pageRequest);

    @Query("select c from Compilation c join fetch c.events " +
            "where c.pinned = ?2")
    List<Compilation> findAllPinnedWithEvents(PageRequest pageRequest, Boolean pinned);
}
