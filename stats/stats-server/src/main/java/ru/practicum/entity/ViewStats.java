package ru.practicum.entity;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {

    private String app;

    private String uri;

    private Long hits;
}
