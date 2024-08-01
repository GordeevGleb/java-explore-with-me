package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Compilation;



@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("select c from Compilation c left join c.events")
    Page<Compilation> getCompilationsWithEvents(PageRequest pageRequest);

    @Query("select c from Compilation c left join c.events where c.pinned = ?1")
    Page<Compilation> getCompilationsWithEventsPinned(PageRequest pageRequest, Boolean pinned);
}
