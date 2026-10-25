package com.banjaraghar.backend.wishlist.service;

import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.catalog.product.service.ProductService;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.user.service.UserService;
import com.banjaraghar.backend.wishlist.repo.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> list(UUID userId) {
        return productService.toSummaries(wishlistItemRepository.findVisibleProducts(userId));
    }

    @Transactional
    public ProductSummaryResponse add(UUID userId, UUID productId) {
        userService.getActiveUser(userId);

        Product product = productRepository.findByIdAndActiveTrueAndCategoryActiveTrue(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        wishlistItemRepository.addIfAbsent(userId, productId);
        return productService.toSummaries(List.of(product)).getFirst();
    }

    @Transactional
    public void remove(UUID userId, UUID productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
}