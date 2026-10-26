package com.banjaraghar.backend.catalog.product.service;

import com.banjaraghar.backend.catalog.category.repo.CategoryRepository;
import com.banjaraghar.backend.catalog.product.dto.response.HomeResponse;
import com.banjaraghar.backend.catalog.product.dto.response.ProductDetailResponse;
import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.enums.ProductSort;
import com.banjaraghar.backend.catalog.product.mapper.ProductMapper;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.catalog.product.repo.ProductSpecifications;
import com.banjaraghar.backend.catalog.product.repo.RelatedProductRepository;
import com.banjaraghar.backend.common.response.PageResponse;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.repo.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MAX_PAGE_SIZE = 48;
    private static final int MAX_SEARCH_WORDS = 5;
    private static final int HOME_LIST_SIZE = 8;
    private static final int RELATED_LIMIT = 4;
    private static final List<OrderStatus> SOLD_STATUSES = List.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RelatedProductRepository relatedProductRepository;
    private final ProductImageService productImageService;
    private final ProductMapper productMapper;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> search(String q, String categorySlug, Long minPrice, Long maxPrice,
                                                       boolean inStockOnly, ProductSort sort, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                sort.getSort());

        UUID categoryId = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            categoryId = categoryRepository.findBySlugAndActiveTrue(categorySlug.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"))
                    .getId();
        }

        List<UUID> matchingIds = null;
        if (q != null && !q.isBlank()) {
            String tsQuery = toPrefixTsQuery(q);
            matchingIds = tsQuery.isEmpty() ? List.of() : productRepository.searchIds(tsQuery);
            if (matchingIds.isEmpty()) {
                return PageResponse.of(Page.empty(pageable), List.of());
            }
        }

        Page<Product> result = productRepository.findAll(
                ProductSpecifications.forShop(categoryId, minPrice, maxPrice, inStockOnly, matchingIds), pageable);
        return PageResponse.of(result, toSummaries(result.getContent()));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug) {
        Product product = getVisibleProduct(slug);
        return productMapper.toDetail(product, productImageService.getImages(product.getId()));
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getRelated(String slug) {
        Product product = getVisibleProduct(slug);

        // Admin-picked products first, then the newest products from the same category fill the gaps
        List<Product> related = new ArrayList<>(relatedProductRepository.findVisibleRelated(product.getId()));
        if (related.size() < RELATED_LIMIT) {
            Set<UUID> excluded = related.stream().map(Product::getId).collect(Collectors.toCollection(HashSet::new));
            excluded.add(product.getId());
            related.addAll(productRepository.findByCategoryIdAndActiveTrueAndIdNotIn(
                    product.getCategory().getId(), excluded,
                    PageRequest.of(0, RELATED_LIMIT - related.size(), ProductSort.NEWEST.getSort())));
        }
        return toSummaries(related.stream().limit(RELATED_LIMIT).toList());
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome() {
        Pageable newestFirst = PageRequest.of(0, HOME_LIST_SIZE, ProductSort.NEWEST.getSort());

        List<ProductSummaryResponse> featured = toSummaries(
                productRepository.findByActiveTrueAndFeaturedTrueAndCategoryActiveTrue(newestFirst));
        List<ProductSummaryResponse> newArrivals = toSummaries(
                productRepository.findByActiveTrueAndCategoryActiveTrue(newestFirst));

        // Empty until the first sale; the website hides an empty section
        return new HomeResponse(featured, newArrivals, getBestSellers());
    }

    private List<ProductSummaryResponse> getBestSellers() {
        // Ask for more than needed, because a top seller may have been hidden since
        List<UUID> rankedIds = orderItemRepository.findBestSellerProductIds(SOLD_STATUSES,
                PageRequest.of(0, HOME_LIST_SIZE * 2));
        if (rankedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Product> visible = productRepository.findVisibleByIdIn(rankedIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> bestSellers = rankedIds.stream()
                .map(visible::get)
                .filter(Objects::nonNull)
                .limit(HOME_LIST_SIZE)
                .toList();
        return toSummaries(bestSellers);
    }

    private Product getVisibleProduct(String slug) {
        return productRepository.findBySlugAndActiveTrueAndCategoryActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public List<ProductSummaryResponse> toSummaries(List<Product> products) {
        Map<UUID, String> images = productImageService.firstImageUrls(products);
        return products.stream()
                .map(product -> productMapper.toSummary(product, images.get(product.getId())))
                .toList();
    }

    private String toPrefixTsQuery(String q) {
        return Arrays.stream(q.trim().split("\\s+"))
                .map(word -> word.replaceAll("[^\\p{L}\\p{M}\\p{N}]", ""))
                .filter(word -> !word.isEmpty())
                .limit(MAX_SEARCH_WORDS)
                .map(word -> word + ":*")
                .collect(Collectors.joining(" & "));
    }
}
