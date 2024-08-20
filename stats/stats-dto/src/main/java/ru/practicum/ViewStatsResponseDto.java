package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsResponseDto {

    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @PositiveOrZero
    private Long hits;
}
