package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.rating.EventShortRatingDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
        private Long id;

        private String annotation;

        private CategoryDto category;

        private Long confirmedRequests;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;

        private UserShortDto initiator;

        private Boolean paid;

        private String title;

        private Long views;

        private EventShortRatingDto eventShortRatingDto;
}
