package com.banjaraghar.backend.catalog.category.controller;

import com.banjaraghar.backend.catalog.category.dto.request.CategoryRequest;
import com.banjaraghar.backend.catalog.category.dto.response.CategoryResponse;
import com.banjaraghar.backend.catalog.category.service.CategoryService;
import com.banjaraghar.backend.common.request.ActiveRequest;
import com.banjaraghar.backend.common.response.ImageUploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.listAll();
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadResponse uploadImage(@RequestParam("file") MultipartFile file) {
        return categoryService.uploadImage(file);
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
