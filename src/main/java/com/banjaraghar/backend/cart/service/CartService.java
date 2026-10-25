package com.banjaraghar.backend.cart.service;

import com.banjaraghar.backend.cart.dto.request.AddToCartRequest;
import com.banjaraghar.backend.cart.dto.request.UpdateCartItemRequest;
import com.banjaraghar.backend.cart.dto.response.CartItemResponse;
import com.banjaraghar.backend.cart.dto.response.CartResponse;
import com.banjaraghar.backend.cart.entity.CartItem;
import com.banjaraghar.backend.cart.repo.CartItemRepository;
import com.banjaraghar.backend.catalog.product.dto.response.ProductSummaryResponse;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.mapper.ProductMapper;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.catalog.product.service.ProductImageService;
import com.banjaraghar.backend.catalog.product.service.ProductService;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final int MAX_QUANTITY_PER_ITEM = 10;
    private static final int SUGGESTION_LIMIT = 4;

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final ProductMapper productMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        return toResponse(cartItemRepository.findByUserIdOrderByProductNameAsc(userId));
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddToCartRequest request) {
        userService.getActiveUser(userId);

        Product product = productRepository.findByIdAndActiveTrueAndCategoryActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        int existing = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                .map(CartItem::getQuantity)
                .orElse(0);
        int newQuantity = existing + request.getQuantity();

        validateQuantity(product, newQuantity);
        cartItemRepository.upsertQuantity(userId, product.getId(), newQuantity);
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateQuantity(UUID userId, UUID productId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        validateQuantity(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.saveAndFlush(item);
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        return getCart(userId);
    }

    @Transactional
    public void clear(UUID userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getSuggestions(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByProductNameAsc(userId);

        List<Product> suggestions;
        if (items.isEmpty()) {
            // Nothing to base suggestions on yet, so show featured products
            suggestions = productRepository
                    .findByActiveTrueAndFeaturedTrueAndCategoryActiveTrue(PageRequest.of(0, SUGGESTION_LIMIT * 2))
                    .stream()
                    .filter(product -> product.getStockQuantity() > 0)
                    .limit(SUGGESTION_LIMIT)
                    .toList();
        } else {
            Set<UUID> productIds = items.stream()
                    .map(item -> item.getProduct().getId())
                    .collect(Collectors.toSet());
            Set<UUID> categoryIds = items.stream()
                    .map(item -> item.getProduct().getCategory().getId())
                    .collect(Collectors.toSet());
            suggestions = productRepository.findSuggestions(productIds, categoryIds,
                    PageRequest.of(0, SUGGESTION_LIMIT));
        }
        return productService.toSummaries(suggestions);
    }

    private void validateQuantity(Product product, int quantity) {
        if (!product.isActive() || !product.getCategory().isActive()) {
            throw new BadRequestException("This product is no longer available");
        }
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new BadRequestException("You can add up to " + MAX_QUANTITY_PER_ITEM + " of one item");
        }
        if (quantity > product.getStockQuantity()) {
            // The exact stock count stays private, so the message does not mention it
            throw new BadRequestException("Not enough stock for the quantity requested");
        }
    }

    private CartResponse toResponse(List<CartItem> items) {
        Map<UUID, String> images = productImageService.firstImageUrls(
                items.stream().map(CartItem::getProduct).toList());

        List<CartItemResponse> responses = items.stream()
                .map(item -> toItemResponse(item, images.get(item.getProduct().getId())))
                .toList();

        int totalQuantity = responses.stream().mapToInt(CartItemResponse::getQuantity).sum();
        long subtotal = responses.stream()
                .filter(CartItemResponse::isAvailable)
                .mapToLong(CartItemResponse::getLineTotal)
                .sum();
        return new CartResponse(responses, totalQuantity, subtotal);
    }

    private CartItemResponse toItemResponse(CartItem item, String imageUrl) {
        Product product = item.getProduct();
        boolean available = product.isActive()
                && product.getCategory().isActive()
                && product.getStockQuantity() >= item.getQuantity();

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                imageUrl,
                product.getPrice(),
                product.getMrp(),
                item.getQuantity(),
                product.getPrice() * item.getQuantity(),
                productMapper.stockStatus(product.getStockQuantity()),
                available);
    }
}