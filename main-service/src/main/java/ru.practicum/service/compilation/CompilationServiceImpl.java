package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.event.EventService;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final EventService eventService;
    private final EntityManager entityManager;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    @Override
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
        CompilationDto savedCompilation = compilationMapper.toCompilationDto(compilationRepository.save(actual));
        log.info("MAIN SERVICE LOG: compilation created");
        return savedCompilation;
    }

    @Override
    public CompilationDto getById(Long id) {
        log.info("MAIN SERVICE LOG: get compilation id " + id);
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
        log.info("MAIN SERVICE LOG: compilation id " + id + " found");
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> get(Boolean pinned, Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get compilations");
        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (Optional.ofNullable(pinned).isPresent() && pinned.equals(Boolean.TRUE)) {
            List<Compilation> resultList = compilationRepository.findByPinned(pageRequest, pinned);
            log.info("MAIN SERVICE LOG: pinned compilation list formed");
            return compilationMapper.toListCompilationDto(resultList);
        }
        List<Compilation> resultList = compilationRepository.findAll(pageRequest).toList();
        log.info("MAIN SERVICE LOG: compilation list formed");
        return compilationMapper.toListCompilationDto(resultList);
    }

    @Override
    public void delete(Long id) {
log.info("MAIN SERVICE LOG: removing compilation id" + id);
Compilation actual = compilationRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
compilationRepository.delete(actual);
log.info("MAIN SERVICE LOG: compilation removed");
    }

    @Override
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
        log.info("MAIN SERVICE LOG: compilation id " + id + " updated");
        return compilationMapper.toCompilationDto(compilationRepository.save(actual));
    }
}
