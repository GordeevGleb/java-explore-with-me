package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN :startTime AND :endTime " +
            "AND e.uri IN :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStats> findAllByTimeAndListOfUris(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN :startTime AND :endTime " +
            "AND e.uri IN :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStats> findAllByTimeAndListOfUrisAndUniqueIp(@Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime,
                                                             @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN :startTime AND :endTime " +
            "GROUP BY e.app, e.uri")
    List<ViewStats> findAllByTime(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Query("SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN :startTime AND :endTime " +
            "GROUP BY e.app, e.uri")
    List<ViewStats> findAllByTimeAndUniqueIp(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

}
