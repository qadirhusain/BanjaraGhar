package com.banjaraghar.backend.content.banner.controller;

import com.banjaraghar.backend.common.request.ActiveRequest;
import com.banjaraghar.backend.content.banner.dto.request.BannerRequest;
import com.banjaraghar.backend.content.banner.dto.response.AdminBannerResponse;
import com.banjaraghar.backend.content.banner.dto.response.BannerImageResponse;
import com.banjaraghar.backend.content.banner.enums.BannerPlacement;
import com.banjaraghar.backend.content.banner.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping
    public List<AdminBannerResponse> list(@RequestParam(required = false) BannerPlacement placement) {
        return bannerService.listAll(placement);
    }

    @GetMapping("/{id}")
    public AdminBannerResponse get(@PathVariable UUID id) {
        return bannerService.get(id);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BannerImageResponse uploadImage(@RequestParam("file") MultipartFile file) {
        return bannerService.uploadImage(file);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminBannerResponse create(@Valid @RequestBody BannerRequest request) {
        return bannerService.create(request);
    }

    @PutMapping("/{id}")
    public AdminBannerResponse update(@PathVariable UUID id, @Valid @RequestBody BannerRequest request) {
        return bannerService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public AdminBannerResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return bannerService.setActive(id, request.getActive());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        bannerService.delete(id);
    }
}