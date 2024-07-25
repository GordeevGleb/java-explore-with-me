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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

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
        List<EventShortDto> compilationEvents = eventMapper.toEventShortDtoList((List<Event>) actual.getEvents());
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
        List<EventShortDto> compilationEvents = eventMapper.toEventShortDtoList((List<Event>) compilation.getEvents());
        log.info("MAIN SERVICE LOG: compilation id " + id + " found");
        return compilationMapper.toCompilationDto(compilation, compilationEvents);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> get(Boolean pinned, Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get compilations");
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<CompilationDto> resultList = new ArrayList<>();
        if (Optional.ofNullable(pinned).isPresent() && pinned.equals(Boolean.TRUE)) {
            List<Compilation> compilations = compilationRepository.findByPinned(pageRequest, pinned);
            for (Compilation compilation : compilations) {
                List<EventShortDto> eventShortDtos = eventMapper
                        .toEventShortDtoList((List<Event>) compilation.getEvents());
                 resultList.add(compilationMapper.toCompilationDto(compilation, eventShortDtos));
            }
            log.info("MAIN SERVICE LOG: pinned compilation list formed");
            return resultList;
        }
        List<Compilation> compilations = compilationRepository.findAll(pageRequest).toList();
        for (Compilation compilation : compilations) {
            List<EventShortDto> eventShortDtos = eventMapper
                    .toEventShortDtoList((List<Event>) compilation.getEvents());
            resultList.add(compilationMapper.toCompilationDto(compilation, eventShortDtos));
        }
        log.info("MAIN SERVICE LOG: compilation list formed");
        return resultList;
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
        List<EventShortDto> eventShortDtos = eventMapper.toEventShortDtoList((List<Event>) actual.getEvents());
        log.info("MAIN SERVICE LOG: compilation id " + id + " updated");
        return compilationMapper.toCompilationDto(actual, eventShortDtos);
    }
}
