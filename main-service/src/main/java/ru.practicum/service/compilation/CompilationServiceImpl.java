package ru.practicum.service.compilation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
//        if (Optional.ofNullable(newCompilationDto.getPinned()).isEmpty()) {
//            actual.setPinned(Boolean.FALSE);
//        }
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
        log.info("MAIN SERVICE LOG: getting compilation list; size: " + size + " from: " + from);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Compilation> query = builder.createQuery(Compilation.class);

        Root<Compilation> root = query.from(Compilation.class);
        Predicate criteria = builder.conjunction();

        if (pinned != null) {
            Predicate isPinned;
            if (pinned) {
                isPinned = builder.isTrue(root.get("pinned"));
            } else {
                isPinned = builder.isFalse(root.get("pinned"));
            }
            criteria = builder.and(criteria, isPinned);
        }

        query.select(root).where(criteria);
        List<Compilation> compilations = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        log.info("MAIN SERVICE LOG: compilation:" + compilations);
        List<CompilationDto> compilationDtos = compilationMapper.toListCompilationDto(compilations);
        log.info("MAIN SERVICE LOG: compilationDto: " + compilationDtos);
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
