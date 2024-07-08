package ru.practicum;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitRequestDto {

    private String app;
    private String uri;
    private String api;
    private LocalDateTime timestamp;
}
