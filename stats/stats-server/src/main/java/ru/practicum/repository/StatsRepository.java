package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.entity.ViewStats(eh.app, eh.uri, count(eh.ip))" +
            "from EndpointHit as eh " +
            "where eh.timestamp between ?1 and ?2 " +
            "group by eh.app, eh.uri " +
            "order by count(eh.ip) desc")
    List<ViewStats> findAll(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.entity.ViewStats(eh.app, eh.uri, count(DISTINCT eh.ip)) " +
            "from EndpointHit as eh " +
            "where eh.timestamp between ?1 and ?2 " +
            "group by eh.app, eh.uri " +
            "order by count(distinct eh.ip) desc")
    List<ViewStats> findUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.entity.ViewStats(eh.app, eh.uri, count(eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.uri in ?3 and eh.timestamp between ?1 and ?2 " +
            "group by eh.app, eh.uri " +
            "order by count(eh.ip) desc")
    List<ViewStats> findByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.entity.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.uri in ?3 and eh.timestamp between ?1 and ?2 " +
            "group by eh.app, eh.uri " +
            "order by count(distinct eh.ip) desc")
    List<ViewStats> findUniqueByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

}
