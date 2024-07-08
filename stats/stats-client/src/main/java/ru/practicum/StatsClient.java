package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class StatsClient {

    private final WebClient webClient;
    private final String serverUrl = "http://stats-server:9090";

    public StatsClient() {
        this.webClient = WebClient.builder().baseUrl(serverUrl).build();
    }

    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return
                webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique).build()).retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                        }).block();
    }

    public Mono<Void> post(EndpointHitDto endpointHitCreateDto) {
        return webClient.post()
                .uri("/hit")
                .body(Mono.just(endpointHitCreateDto), EndpointHitDto.class)
                .retrieve().bodyToMono(Void.class);
    }
}
