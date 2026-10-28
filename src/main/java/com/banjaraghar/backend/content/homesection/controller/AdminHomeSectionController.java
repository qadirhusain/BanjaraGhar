package com.banjaraghar.backend.content.homesection.controller;

import com.banjaraghar.backend.common.request.ActiveRequest;
import com.banjaraghar.backend.content.homesection.dto.request.HomeSectionRequest;
import com.banjaraghar.backend.content.homesection.dto.request.SectionProductsRequest;
import com.banjaraghar.backend.content.homesection.dto.response.AdminHomeSectionResponse;
import com.banjaraghar.backend.content.homesection.service.HomeSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activitylog/home-sections")
@RequiredArgsConstructor
public class AdminHomeSectionController {

    private final HomeSectionService sectionService;

    @GetMapping
    public List<AdminHomeSectionResponse> list() {
        return sectionService.listAll();
    }

    @GetMapping("/{id}")
    public AdminHomeSectionResponse get(@PathVariable UUID id) {
        return sectionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminHomeSectionResponse create(@Valid @RequestBody HomeSectionRequest request) {
        return sectionService.create(request);
    }

    @PutMapping("/{id}")
    public AdminHomeSectionResponse update(@PathVariable UUID id, @Valid @RequestBody HomeSectionRequest request) {
        return sectionService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public AdminHomeSectionResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return sectionService.setActive(id, request.getActive());
    }

    @PutMapping("/{id}/products")
    public AdminHomeSectionResponse setProducts(@PathVariable UUID id,
                                                @Valid @RequestBody SectionProductsRequest request) {
        return sectionService.setProducts(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sectionService.delete(id);
    }
}