package ru.practicum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import java.util.*;

@Service
@Slf4j
public class StatsClient {
    private final String serverUrl;
    private final RestTemplate restTemplate;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.restTemplate = builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void addStats(EndpointHitRequestDto endpointHitDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitRequestDto> requestEntity = new HttpEntity<>(endpointHitDto, headers);
        EndpointHitRequestDto endpointHitRequestDto = restTemplate
                .exchange(serverUrl + "/hit/",
                        HttpMethod.POST, requestEntity, EndpointHitRequestDto.class).getBody();
        log.info("STATS SERVER: requestDto " + endpointHitRequestDto.toString());
    }

    public List<ViewStatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("params: start " + start + " end " + end + " uris " + uris + "unique " + unique);
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("uris", uris);
        parameters.put("unique", unique);

        ResponseEntity<String> response = restTemplate.getForEntity(
                serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                String.class, parameters);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return Arrays.asList(objectMapper.readValue(response.getBody(), ViewStatsResponseDto[].class));
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Json processing error: " + exception.getMessage());
        }
    }
}
