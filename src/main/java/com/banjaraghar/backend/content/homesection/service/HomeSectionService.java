package com.banjaraghar.backend.content.homesection.service;

import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.catalog.product.service.ProductService;
import com.banjaraghar.backend.content.homesection.dto.request.HomeSectionRequest;
import com.banjaraghar.backend.content.homesection.dto.request.SectionProductsRequest;
import com.banjaraghar.backend.content.homesection.dto.response.AdminHomeSectionResponse;
import com.banjaraghar.backend.content.homesection.dto.response.HomeSectionResponse;
import com.banjaraghar.backend.content.homesection.entity.HomeSection;
import com.banjaraghar.backend.content.homesection.entity.HomeSectionProduct;
import com.banjaraghar.backend.content.homesection.repo.HomeSectionProductRepository;
import com.banjaraghar.backend.content.homesection.repo.HomeSectionRepository;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeSectionService {

    private final HomeSectionRepository sectionRepository;
    private final HomeSectionProductRepository sectionProductRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // ------------------------------------------------------------------ public

    @Transactional(readOnly = true)
    public List<HomeSectionResponse> getHomeSections() {
        List<HomeSection> sections = sectionRepository.findByActiveTrueOrderBySortOrderAscCreatedAtAsc();
        if (sections.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<UUID>> idsBySection = productIdsBySection(sections);

        // Hidden and out-of-category products are dropped here, so the page never links to a dead product
        Set<UUID> allIds = idsBySection.values().stream().flatMap(List::stream).collect(Collectors.toSet());
        List<Product> visible = allIds.isEmpty() ? List.of() : productRepository.findVisibleByIdIn(allIds);
        Map<UUID, ProductSummaryResponse> summaries = productService.toSummaries(visible).stream()
                .collect(Collectors.toMap(ProductSummaryResponse::getId, Function.identity()));

        List<HomeSectionResponse> result = new ArrayList<>();
        for (HomeSection section : sections) {
            List<ProductSummaryResponse> products = idsBySection.get(section.getId()).stream()
                    .map(summaries::get)
                    .filter(Objects::nonNull)
                    .toList();
            if (!products.isEmpty()) {          // an empty section would only show a blank heading
                result.add(new HomeSectionResponse(section.getId(), section.getTitle(), section.getSubtitle(),
                        section.getLayout(), products));
            }
        }
        return result;
    }

    // ------------------------------------------------------------------- activitylog

    @Transactional(readOnly = true)
    public List<AdminHomeSectionResponse> listAll() {
        List<HomeSection> sections = sectionRepository.findAllByOrderBySortOrderAscCreatedAtAsc();
        Map<UUID, List<UUID>> idsBySection = productIdsBySection(sections);
        return sections.stream()
                .map(section -> toAdminResponse(section, idsBySection.get(section.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminHomeSectionResponse get(UUID id) {
        HomeSection section = getSection(id);
        return toAdminResponse(section, productIdsBySection(List.of(section)).get(id));
    }

    @Transactional
    public AdminHomeSectionResponse create(HomeSectionRequest request) {
        HomeSection section = new HomeSection();
        section.setActive(true);
        apply(section, request);
        HomeSection saved = sectionRepository.save(section);
        return toAdminResponse(saved, List.of());
    }

    @Transactional
    public AdminHomeSectionResponse update(UUID id, HomeSectionRequest request) {
        HomeSection section = getSection(id);
        apply(section, request);
        HomeSection saved = sectionRepository.save(section);
        return toAdminResponse(saved, productIdsBySection(List.of(saved)).get(id));
    }

    @Transactional
    public AdminHomeSectionResponse setActive(UUID id, boolean active) {
        HomeSection section = getSection(id);
        section.setActive(active);
        HomeSection saved = sectionRepository.save(section);
        return toAdminResponse(saved, productIdsBySection(List.of(saved)).get(id));
    }

    /** Replaces the products of a section with the given list, in the given order. */
    @Transactional
    public AdminHomeSectionResponse setProducts(UUID id, SectionProductsRequest request) {
        HomeSection section = getSection(id);

        List<UUID> productIds = request.getProductIds();
        if (new HashSet<>(productIds).size() != productIds.size()) {
            throw new BadRequestException("The same product cannot be added twice");
        }
        if (productRepository.findAllById(productIds).size() != productIds.size()) {
            throw new ResourceNotFoundException("One or more products were not found");
        }

        sectionProductRepository.deleteBySectionId(id);
        List<HomeSectionProduct> links = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            links.add(new HomeSectionProduct(id, productIds.get(i), i));
        }
        sectionProductRepository.saveAll(links);

        return toAdminResponse(section, productIds);
    }

    @Transactional
    public void delete(UUID id) {
        sectionRepository.delete(getSection(id));      // its product links are removed by the database
    }

    // ----------------------------------------------------------------- helpers

    private HomeSection getSection(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Home section not found"));
    }

    private void apply(HomeSection section, HomeSectionRequest request) {
        section.setTitle(request.getTitle().trim());
        section.setSubtitle(request.getSubtitle() == null || request.getSubtitle().isBlank()
                ? null : request.getSubtitle().trim());
        section.setLayout(request.getLayout());
        section.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    /** Product ids of each section in display order; every section gets an entry, even if empty. */
    private Map<UUID, List<UUID>> productIdsBySection(List<HomeSection> sections) {
        Map<UUID, List<UUID>> result = new HashMap<>();
        sections.forEach(section -> result.put(section.getId(), new ArrayList<>()));
        if (sections.isEmpty()) {
            return result;
        }
        List<UUID> sectionIds = sections.stream().map(HomeSection::getId).toList();
        for (HomeSectionProduct link : sectionProductRepository.findBySectionIdInOrderBySortOrderAsc(sectionIds)) {
            result.get(link.getSectionId()).add(link.getProductId());
        }
        return result;
    }

    private AdminHomeSectionResponse toAdminResponse(HomeSection section, List<UUID> productIds) {
        return new AdminHomeSectionResponse(section.getId(), section.getTitle(), section.getSubtitle(),
                section.getLayout(), section.getSortOrder(), section.isActive(), productIds, section.getCreatedAt());
    }
}