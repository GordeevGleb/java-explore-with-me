package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ViewStatsResponseDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {


    @Query(value = "SELECT * FROM ( " +
            "SELECT distinct on (s.uri) s.app, s.uri , ss.hits from ( " +
            " SELECT uri, count(uri) as hits " +
            " FROM stats WHERE timestamp >= ?1 and timestamp <= ?2 GROUP BY uri ) ss " +
            "JOIN endpointhits s on s.uri = ss.uri ) t ORDER BY t.hits DESC;", nativeQuery = true)
    List<ViewStats> findEndpointHits(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM ( " +
            "SELECT distinct on (ss.uri) s.app, ss.uri, ss.hits FROM ( " +
            "SELECT uri, count(distinct ip) as hits from stats WHERE timestamp >= ?1 and timestamp <= ?2 " +
            "group by uri ORDER BY hits DESC " +
            ") ss JOIN stats s on s.uri = ss.uri ) t ORDER BY t.hits DESC;", nativeQuery = true)
    List<ViewStats> findEndpointHitsWithUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM ( " +
            "SELECT distinct on (s.uri) s.app, s.uri , ss.hits from ( " +
            " SELECT uri, count(uri) as hits " +
            " FROM stats WHERE uri in ?3 and timestamp >= ?1 and timestamp <= ?2 GROUP BY uri ) ss " +
            "JOIN stats s on s.uri = ss.uri ) t ORDER BY t.hits DESC;", nativeQuery = true)
    List<ViewStats> findEndpointHitsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "SELECT * FROM ( " +
            "SELECT distinct on (ss.uri) s.app,ss.uri, ss.hits FROM ( " +
            "SELECT uri, count(distinct ip) as hits from stats WHERE uri in ?3 and timestamp >= ?1 " +
            "and timestamp <= ?2 group by uri ORDER BY hits DESC " +
            ") ss JOIN stats s on s.uri = ss.uri ) t ORDER BY t.hits DESC;", nativeQuery = true)
    List<ViewStats> findEndpointHitsWithUniqueIpWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

}
