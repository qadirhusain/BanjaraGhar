package com.banjaraghar.backend.catalog.product.service;

import com.banjaraghar.backend.catalog.category.entity.Category;
import com.banjaraghar.backend.catalog.category.repo.CategoryRepository;
import com.banjaraghar.backend.catalog.product.dto.request.ProductRequest;
import com.banjaraghar.backend.catalog.product.dto.request.RelatedProductsRequest;
import com.banjaraghar.backend.catalog.product.dto.response.AdminProductResponse;
import com.banjaraghar.backend.catalog.product.dto.response.AdminProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.entity.RelatedProduct;
import com.banjaraghar.backend.catalog.product.mapper.ProductMapper;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.catalog.product.repo.RelatedProductRepository;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.common.util.SlugUtil;
import com.banjaraghar.backend.common.util.TextUtil;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ConflictException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RelatedProductRepository relatedProductRepository;
    private final ProductImageService productImageService;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public PageResponse<AdminProductSummaryResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> result = productRepository.findAll(pageable);

        Map<UUID, String> images = productImageService.firstImageUrls(result.getContent());
        List<AdminProductSummaryResponse> content = result.getContent().stream()
                .map(product -> productMapper.toAdminSummary(product, images.get(product.getId())))
                .toList();
        return PageResponse.of(result, content);
    }

    @Transactional(readOnly = true)
    public AdminProductResponse get(UUID id) {
        return toResponse(getProduct(id));
    }

    @Transactional
    public AdminProductResponse create(ProductRequest request) {
        validate(request, null);
        Category category = getCategory(request.getCategoryId());

        Product product = new Product();
        product.setSlug(uniqueSlug(request.getName()));
        apply(product, request, category);
        // A new product starts hidden with no stock, until the activitylog adds photos and publishes it
        product.setStockQuantity(0);
        product.setFeatured(false);
        product.setActive(false);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse update(UUID id, ProductRequest request) {
        Product product = getProduct(id);
        validate(request, id);
        Category category = getCategory(request.getCategoryId());

        apply(product, request, category);   // the slug stays the same so existing links keep working
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse setActive(UUID id, boolean active) {
        Product product = getProduct(id);
        product.setActive(active);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse setStock(UUID id, int quantity) {
        Product product = getProduct(id);
        product.setStockQuantity(quantity);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse setFeatured(UUID id, boolean featured) {
        Product product = getProduct(id);
        product.setFeatured(featured);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public AdminProductResponse setRelated(UUID id, RelatedProductsRequest request) {
        Product product = getProduct(id);
        List<UUID> ids = request.getProductIds();

        if (new HashSet<>(ids).size() != ids.size()) {
            throw new BadRequestException("A product can be picked only once");
        }
        if (ids.contains(id)) {
            throw new BadRequestException("A product cannot be related to itself");
        }

        Map<UUID, Product> found = ids.isEmpty() ? Map.of() : productRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (found.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more related products were not found");
        }

        relatedProductRepository.clearForProduct(id);

        List<RelatedProduct> picks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            RelatedProduct pick = new RelatedProduct();
            pick.setProduct(product);
            pick.setRelatedProduct(found.get(ids.get(i)));
            pick.setSortOrder(i);
            picks.add(pick);
        }
        relatedProductRepository.saveAll(picks);
        return toResponse(product);
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private void validate(ProductRequest request, UUID currentProductId) {
        if (request.getMrp() < request.getPrice()) {
            throw new BadRequestException("MRP cannot be lower than the selling price");
        }

        String sku = request.getSku().trim();
        boolean taken = currentProductId == null
                ? productRepository.existsBySkuIgnoreCase(sku)
                : productRepository.existsBySkuIgnoreCaseAndIdNot(sku, currentProductId);
        if (taken) {
            throw new ConflictException("A product with this SKU already exists");
        }
    }

    private String uniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String slug = base;
        int suffix = 2;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private void apply(Product product, ProductRequest request, Category category) {
        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setMrp(request.getMrp());
        product.setMaterial(TextUtil.blankToNull(request.getMaterial()));
        product.setColour(TextUtil.blankToNull(request.getColour()));
        product.setSize(TextUtil.blankToNull(request.getSize()));
        product.setCareInstructions(TextUtil.blankToNull(request.getCareInstructions()));
        product.setSku(request.getSku().trim());
        product.setPackedWeightG(request.getPackedWeightG());
        product.setPackedLengthCm(request.getPackedLengthCm());
        product.setPackedBreadthCm(request.getPackedBreadthCm());
        product.setPackedHeightCm(request.getPackedHeightCm());
    }

    private AdminProductResponse toResponse(Product product) {
        return productMapper.toAdmin(
                product,
                productImageService.getImages(product.getId()),
                relatedProductRepository.findRelatedIds(product.getId()));
    }
}