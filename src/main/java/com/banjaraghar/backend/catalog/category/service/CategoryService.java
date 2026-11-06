package com.banjaraghar.backend.catalog.category.service;

import com.banjaraghar.backend.catalog.category.dto.request.CategoryRequest;
import com.banjaraghar.backend.catalog.category.dto.response.CategoryResponse;
import com.banjaraghar.backend.catalog.category.entity.Category;
import com.banjaraghar.backend.catalog.category.repo.CategoryRepository;
import com.banjaraghar.backend.common.response.ImageUploadResponse;
import com.banjaraghar.backend.common.util.SlugUtil;
import com.banjaraghar.backend.common.util.TextUtil;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String STORAGE_FOLDER = "categories";

    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /** Stores a category picture and returns its url, to be used in create or update. */
    public ImageUploadResponse uploadImage(MultipartFile file) {
        return new ImageUploadResponse(fileStorageService.storeImage(file, STORAGE_FOLDER));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A category with this name already exists");
        }

        Category category = new Category();
        category.setSlug(uniqueSlug(name));
        apply(category, request);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = getCategory(id);

        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("A category with this name already exists");
        }

        String oldImage = category.getImage();
        apply(category, request);   // the slug stays the same so existing links keep working
        Category saved = categoryRepository.save(category);

        // A replaced picture is no longer needed
        if (!Objects.equals(oldImage, saved.getImage())) {
            fileStorageService.delete(oldImage);
        }
        return toResponse(saved);
    }

    @Transactional
    public CategoryResponse setActive(UUID id, boolean active) {
        Category category = getCategory(id);
        category.setActive(active);
        return toResponse(categoryRepository.save(category));
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private String uniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String slug = base;
        int suffix = 2;
        while (categoryRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private void apply(Category category, CategoryRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(TextUtil.blankToNull(request.getDescription()));
        category.setImage(TextUtil.blankToNull(request.getImage()));
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getImage(),
                category.getSortOrder(),
                category.isActive());
    }
}