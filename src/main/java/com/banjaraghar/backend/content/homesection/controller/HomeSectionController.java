package com.banjaraghar.backend.content.homesection.controller;

import com.banjaraghar.backend.content.homesection.dto.response.HomeSectionResponse;
import com.banjaraghar.backend.content.homesection.service.HomeSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Under /api/home, which SecurityConfig already makes public for GET
@RestController
@RequestMapping("/api/home/sections")
@RequiredArgsConstructor
public class HomeSectionController {

    private final HomeSectionService sectionService;

    @GetMapping
    public List<HomeSectionResponse> getSections() {
        return sectionService.getHomeSections();
    }
}