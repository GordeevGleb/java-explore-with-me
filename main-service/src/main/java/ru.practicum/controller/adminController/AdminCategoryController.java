package ru.practicum.controller.adminController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.category.CategoryService;


@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryDto categoryRequestDto) {
        return categoryService.create(categoryRequestDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(name = "catId") Long id) {
        categoryService.delete(id);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable(name = "catId") Long id, @Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.update(id, newCategoryDto);
    }
}
