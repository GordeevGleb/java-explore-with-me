package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    private final EntityManager entityManager;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        log.info("MAIN SERVICE LOG: creating new compilation");
        List<Event> events;
        if (Optional.ofNullable(newCompilationDto.getEvents()).isPresent()) {
            events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
        } else {
            events = new ArrayList<>();
        }
        Compilation actual = compilationMapper.toCompilation(newCompilationDto, new HashSet<>(events));
        if (Optional.ofNullable(newCompilationDto.getPinned()).isEmpty()) {
            actual.setPinned(Boolean.FALSE);
        }
        actual = compilationRepository.save(actual);
        List<EventShortDto> compilationEvents = actual
                .getEvents()
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
        CompilationDto savedCompilation = compilationMapper.toCompilationDto(actual, compilationEvents);
        log.info("MAIN SERVICE LOG: compilation created");
        return savedCompilation;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long id) {
        log.info("MAIN SERVICE LOG: get compilation id " + id);
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
        List<EventShortDto> compilationEvents = compilation
                .getEvents()
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
        log.info("MAIN SERVICE LOG: compilation id " + id + " found");
        return compilationMapper.toCompilationDto(compilation, compilationEvents);
    }

    @Override
    public List<CompilationDto> get(Boolean pinned, Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: getting compilation list");
        List<CompilationDto> compilationDtos = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        if (pinned) {
            List<Compilation> compilations = compilationRepository
                    .getCompilationsWithEventsPinned(pageRequest, pinned).toList();
            for (Compilation compilation : compilations) {
                List<Event> events = new ArrayList<>(compilation.getEvents());
                List<EventShortDto> eventShortDtos = eventMapper.toEventShortDtoList(events);
                compilationDtos.add(compilationMapper.toCompilationDto(compilation, eventShortDtos));
            }
        } else {
            List<Compilation> compilations = compilationRepository
                    .getCompilationsWithEvents(pageRequest).toList();
            for (Compilation compilation : compilations) {
                List<Event> events = new ArrayList<>(compilation.getEvents());
                List<EventShortDto> eventShortDtos = eventMapper.toEventShortDtoList(events);
                compilationDtos.add(compilationMapper.toCompilationDto(compilation, eventShortDtos));
            }
        }
        log.info("MAIN SERVICE LOG: compilation list formed; size: {}", compilationDtos.size());
        return compilationDtos;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("MAIN SERVICE LOG: removing compilation id" + id);
        Compilation actual = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
        compilationRepository.delete(actual);
        log.info("MAIN SERVICE LOG: compilation removed");
    }

    @Override
    @Transactional
    public CompilationDto update(Long id, UpdateCompilationRequest updateCompilationRequest) {
        log.info("MAIN SERVICE LOG: updating compilation id " + id);
        Compilation actual = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
            actual.setEvents(new HashSet<>(events));
        }
        if (updateCompilationRequest.getPinned() != null) {
            actual.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            actual.setTitle(updateCompilationRequest.getTitle());
        }
        actual = compilationRepository.save(actual);
        List<EventShortDto> eventShortDtos = actual
                .getEvents()
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
        log.info("MAIN SERVICE LOG: compilation id " + id + " updated");
        return compilationMapper.toCompilationDto(actual, eventShortDtos);
    }
}
