package com.banjaraghar.backend.content.banner.controller;

import com.banjaraghar.backend.content.banner.dto.response.BannerResponse;
import com.banjaraghar.backend.content.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Both paths sit under /api/home and /api/categories, which SecurityConfig already makes public for GET
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/home/banners")
    public List<BannerResponse> homeBanners() {
        return bannerService.getHomeBanners();
    }

    @GetMapping("/categories/{slug}/banners")
    public List<BannerResponse> categoryBanners(@PathVariable String slug) {
        return bannerService.getCategoryBanners(slug);
    }
}