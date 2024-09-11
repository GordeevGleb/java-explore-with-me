package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.Category;
import ru.practicum.exception.CategoryNotEmptyException;
import ru.practicum.exception.ConcurrentNameException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;


    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        log.info("MAIN SERVICE LOG: creating category " + newCategoryDto.getName());
        if (validateByName(newCategoryDto.getName())) {
            throw new ConcurrentNameException("could not execute statement; SQL [n/a];" +
                    " constraint [uq_category_name];" +
                    " nested exception is org.hibernate.exception.ConstraintViolationException:" +
                    " could not execute statement");
        }
        Category actual = categoryRepository.save(categoryMapper.toCategory(newCategoryDto));
        log.info("MAIN SERVICE LOG: category created");
        return categoryMapper.toCategoryDto(actual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> get(Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get categories");
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Category> actual = categoryRepository.findAll(pageRequest).toList();
        log.info("MAIN SERVICE LOG: categries list formed");
        return categoryMapper.toCategoryDtoList(actual);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        log.info("MAIN SERVICE LOG: get category by id " + id);
        if (!validateById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
        Category category = categoryRepository.findById(id).get();
        log.info("MAIN SERVICE LOG: category id " + id + " found");
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("MAIN SERVICE LOG: removing category id " + id);
        if (validateById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
        Category actual = categoryRepository.findById(id).get();
        if (eventRepository.existsByCategoryId(id)) {
            throw new CategoryNotEmptyException("The category is not empty");
        }
        categoryRepository.delete(actual);
        log.info("MAIN SERVICE LOG: category removed");
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, NewCategoryDto newCategoryDto) {
        log.info("MAIN SERVICE LOG: updating category id " + id);
        if (validateById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
        Category actual = categoryRepository.findById(id).get();
        if (categoryRepository.existsByName(newCategoryDto.getName()) &&
                !actual.getName().equals(newCategoryDto.getName())) {
            throw new ConcurrentNameException("could not execute statement; SQL [n/a];" +
                    " constraint [uq_category_name];" +
                    " nested exception is org.hibernate.exception.ConstraintViolationException:" +
                    " could not execute statement");
        }
        actual.setName(newCategoryDto.getName());
        Category updatedCategory = categoryRepository.save(actual);
        log.info("MAIN SERVICE LOG: category id " + id + " updated");
        return categoryMapper.toCategoryDto(updatedCategory);
    }

    private Boolean validateById(Long id) {
        return categoryRepository.existsById(id);
    }

    private Boolean validateByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
