package ru.practicum.dto.rating;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.user.UserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutRatingShortDto {

    @NotNull
    private Long id;

    @NotNull
    private UserDto user;

    @NotNull
    private Boolean isLiked;

    private Float locationRate;

    private Float organizationRate;

    private Float contentRate;
}
