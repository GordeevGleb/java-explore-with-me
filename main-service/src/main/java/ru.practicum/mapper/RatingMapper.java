package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.rating.IncRatingDto;
import ru.practicum.dto.rating.OutRatingDto;
import ru.practicum.dto.rating.OutRatingShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.Rating;
import ru.practicum.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(target = "id", source = "rating.id")
    @Mapping(target = "user", source = "userDto")
    @Mapping(target = "event", source = "eventShortDto")
    OutRatingDto toOutRatingDto(Rating rating, UserDto userDto, EventShortDto eventShortDto);

    @Mapping(target = "id", source = "rating.id")
    @Mapping(target = "user", source = "userDto")
    OutRatingShortDto toOutRatingShortDto(Rating rating, UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "isLiked", source = "incRatingDto.isLiked")
    @Mapping(target = "locationRate", source = "incRatingDto.locationRate")
    @Mapping(target = "organizationRate", source = "incRatingDto.organizationRate")
    @Mapping(target = "contentRate", source = "incRatingDto.contentRate")
    @Mapping(target = "updateTime", source = "localDateTime")
    Rating toRating(User user, Event event, IncRatingDto incRatingDto, LocalDateTime localDateTime);

    List<OutRatingDto> toOutRatingDtoList(List<Rating> ratings);
}
