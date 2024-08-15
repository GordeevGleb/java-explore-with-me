package ru.practicum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class StatsClient {
    private final String serverUrl;
    private final RestTemplate restTemplate;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }

    public void addStats(EndpointHitRequestDto endpointHitDto) {
        restTemplate.postForObject("/hit", endpointHitDto, Void.class);
        log.info("STATS SERVER: requestDto " + endpointHitDto.toString());
    }

    public List<ViewStatsResponseDto> getStats(LocalDateTime start,
                                               LocalDateTime end, List<String> uris, Boolean unique) {
        String formattedStart = start.format(formatter);
        String formattedEnd = end.format(formatter);

        StringBuilder urlBuilder = new StringBuilder("/stats?");
        urlBuilder.append("start=").append(formattedStart);
        urlBuilder.append("&end=").append(formattedEnd);
        urlBuilder.append("&unique=").append(unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                urlBuilder.append("&uris=").append(uri);
            }
        }

        String url = urlBuilder.toString();

        ResponseEntity<List<ViewStatsResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStatsResponseDto>>() {}
        );

        return response.getBody();
    }
}
