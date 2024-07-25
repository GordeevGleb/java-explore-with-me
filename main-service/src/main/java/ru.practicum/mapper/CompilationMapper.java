package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(target = "events", source = "events")
    CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events);
@Mapping(target = "events", source = "events")
    List<CompilationDto> toListCompilationDto(List<Compilation> compilations);

    @Mapping(target = "events", source = "events")
    Compilation toCompilation(NewCompilationDto newCompilationDto, HashSet<Event> events);
}
