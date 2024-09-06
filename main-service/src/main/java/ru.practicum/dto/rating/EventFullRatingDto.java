package ru.practicum.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullRatingDto {


    private Integer percentRating;

    private Float locationRate;

    private Float organizationRate;

    private Float contentRate;
}
