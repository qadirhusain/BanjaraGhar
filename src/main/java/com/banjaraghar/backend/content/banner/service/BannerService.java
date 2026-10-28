package com.banjaraghar.backend.content.banner.service;

import com.banjaraghar.backend.catalog.category.entity.Category;
import com.banjaraghar.backend.catalog.category.repo.CategoryRepository;
import com.banjaraghar.backend.content.banner.dto.request.BannerRequest;
import com.banjaraghar.backend.content.banner.dto.response.AdminBannerResponse;
import com.banjaraghar.backend.content.banner.dto.response.BannerImageResponse;
import com.banjaraghar.backend.content.banner.dto.response.BannerResponse;
import com.banjaraghar.backend.content.banner.entity.Banner;
import com.banjaraghar.backend.content.banner.enums.BannerPlacement;
import com.banjaraghar.backend.content.banner.repo.BannerRepository;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BannerService {

    private static final String STORAGE_FOLDER = "banners";

    private final BannerRepository bannerRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    // ------------------------------------------------------------------ public

    @Transactional(readOnly = true)
    public List<BannerResponse> getHomeBanners() {
        return bannerRepository.findLive(BannerPlacement.HOME, Instant.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> getCategoryBanners(String categorySlug) {
        Category category = categoryRepository.findBySlugAndActiveTrue(categorySlug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return bannerRepository.findLiveForCategory(category.getId(), Instant.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------- activitylog

    @Transactional(readOnly = true)
    public List<AdminBannerResponse> listAll(BannerPlacement placement) {
        List<Banner> banners = placement == null
                ? bannerRepository.findAllByOrderByPlacementAscSortOrderAscCreatedAtDesc()
                : bannerRepository.findByPlacementOrderBySortOrderAscCreatedAtDesc(placement);
        return banners.stream().map(this::toAdminResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminBannerResponse get(UUID id) {
        return toAdminResponse(getBanner(id));
    }

    /** Stores a banner picture and returns its url, to be used in create or update. */
    public BannerImageResponse uploadImage(MultipartFile file) {
        return new BannerImageResponse(fileStorageService.storeImage(file, STORAGE_FOLDER));
    }

    @Transactional
    public AdminBannerResponse create(BannerRequest request) {
        Banner banner = new Banner();
        banner.setActive(true);
        apply(banner, request);
        return toAdminResponse(bannerRepository.save(banner));
    }

    @Transactional
    public AdminBannerResponse update(UUID id, BannerRequest request) {
        Banner banner = getBanner(id);
        String oldImage = banner.getImageUrl();
        String oldMobileImage = banner.getMobileImageUrl();

        apply(banner, request);
        Banner saved = bannerRepository.save(banner);

        // A replaced picture is no longer needed
        if (!Objects.equals(oldImage, saved.getImageUrl())) {
            fileStorageService.delete(oldImage);
        }
        if (!Objects.equals(oldMobileImage, saved.getMobileImageUrl())) {
            fileStorageService.delete(oldMobileImage);
        }
        return toAdminResponse(saved);
    }

    @Transactional
    public AdminBannerResponse setActive(UUID id, boolean active) {
        Banner banner = getBanner(id);
        banner.setActive(active);
        return toAdminResponse(bannerRepository.save(banner));
    }

    @Transactional
    public void delete(UUID id) {
        Banner banner = getBanner(id);
        bannerRepository.delete(banner);
        fileStorageService.delete(banner.getImageUrl());
        fileStorageService.delete(banner.getMobileImageUrl());
    }

    // ----------------------------------------------------------------- helpers

    private Banner getBanner(UUID id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found"));
    }

    private void apply(Banner banner, BannerRequest request) {
        if (request.getStartsAt() != null && request.getEndsAt() != null
                && !request.getStartsAt().isBefore(request.getEndsAt())) {
            throw new BadRequestException("The start date must be before the end date");
        }

        if (request.getPlacement() == BannerPlacement.CATEGORY) {
            if (request.getCategoryId() == null) {
                throw new BadRequestException("Choose the category this banner belongs to");
            }
            banner.setCategory(categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
        } else {
            if (request.getCategoryId() != null) {
                throw new BadRequestException("A home page banner cannot belong to a category");
            }
            banner.setCategory(null);
        }

        banner.setPlacement(request.getPlacement());
        banner.setTitle(blankToNull(request.getTitle()));
        banner.setSubtitle(blankToNull(request.getSubtitle()));
        banner.setButtonText(blankToNull(request.getButtonText()));
        banner.setLinkUrl(blankToNull(request.getLinkUrl()));
        banner.setImageUrl(request.getImageUrl().trim());
        banner.setMobileImageUrl(blankToNull(request.getMobileImageUrl()));
        banner.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        banner.setStartsAt(request.getStartsAt());
        banner.setEndsAt(request.getEndsAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BannerResponse toResponse(Banner banner) {
        return new BannerResponse(banner.getId(), banner.getTitle(), banner.getSubtitle(), banner.getButtonText(),
                banner.getLinkUrl(), banner.getImageUrl(), banner.getMobileImageUrl());
    }

    private AdminBannerResponse toAdminResponse(Banner banner) {
        Category category = banner.getCategory();
        return new AdminBannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getButtonText(),
                banner.getLinkUrl(),
                banner.getImageUrl(),
                banner.getMobileImageUrl(),
                banner.getPlacement(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                banner.getSortOrder(),
                banner.isActive(),
                banner.getStartsAt(),
                banner.getEndsAt(),
                banner.getCreatedAt());
    }
}