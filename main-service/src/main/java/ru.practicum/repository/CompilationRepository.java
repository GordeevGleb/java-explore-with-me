package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Compilation;


@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("select c FROM Compilation c join c.events")
    Page<Compilation> findAllWithEvents(PageRequest pageRequest);

    @Query("select c from Compilation c join c.events " +
            "where c.pinned = ?1")
    Page<Compilation> findAllPinnedWithEvents(PageRequest pageRequest, Boolean pinned);
}
