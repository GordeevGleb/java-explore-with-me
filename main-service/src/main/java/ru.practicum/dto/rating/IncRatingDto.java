package ru.practicum.dto.rating;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncRatingDto {

    @NotNull
    private Boolean isLiked;

    private Float locationRate;

    private Float organizationRate;

    private Float contentRate;
}
