package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    List<CategoryDto> get(Integer from, Integer size);

    CategoryDto getById(Long id);

    void delete(Long id);

    CategoryDto update(Long catId, NewCategoryDto newCategoryDto);
}
