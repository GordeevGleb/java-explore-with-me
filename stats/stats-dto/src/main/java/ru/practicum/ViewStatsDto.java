package ru.practicum;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {
    private String app;
    private String uri;
    private Integer hits;
}
