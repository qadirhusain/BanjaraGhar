package com.banjaraghar.backend.catalog.category.controller;

import com.banjaraghar.backend.catalog.category.dto.request.CategoryRequest;
import com.banjaraghar.backend.catalog.category.dto.response.CategoryResponse;
import com.banjaraghar.backend.catalog.category.service.CategoryService;
import com.banjaraghar.backend.common.request.ActiveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/activitylog/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public CategoryResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return categoryService.setActive(id, request.getActive());
    }
}
