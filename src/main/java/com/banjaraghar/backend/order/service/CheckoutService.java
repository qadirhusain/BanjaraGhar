package com.banjaraghar.backend.order.service;

import com.banjaraghar.backend.cart.entity.CartItem;
import com.banjaraghar.backend.cart.repo.CartItemRepository;
import com.banjaraghar.backend.catalog.product.entity.Product;
import com.banjaraghar.backend.catalog.product.repo.ProductImageRepository;
import com.banjaraghar.backend.catalog.product.repo.ProductRepository;
import com.banjaraghar.backend.catalog.product.service.ProductImageService;
import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.coupon.entity.Coupon;
import com.banjaraghar.backend.coupon.service.CouponService;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.order.dto.request.CheckoutRequest;
import com.banjaraghar.backend.order.dto.response.CheckoutResponse;
import com.banjaraghar.backend.order.entity.Order;
import com.banjaraghar.backend.order.entity.OrderItem;
import com.banjaraghar.backend.order.enums.OrderStatus;
import com.banjaraghar.backend.order.repo.OrderRepository;
import com.banjaraghar.backend.payment.client.RazorpayGateway;
import com.banjaraghar.backend.user.entity.Address;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.repo.AddressRepository;
import com.banjaraghar.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final int PAYMENT_WINDOW_MINUTES = 30;
    private static final long MIN_PAYABLE_PAISE = 100;

    private final UserService userService;
    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ShippingFeeService shippingFeeService;
    private final RazorpayGateway razorpayGateway;
    private final AppProperties appProperties;

    @Transactional
    public CheckoutResponse checkout(UUID userId, CheckoutRequest request) {
        User user = userService.getActiveUser(userId);
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING_PAYMENT)
                .forEach(pending -> orderService.releaseUnpaid(pending.getId(), false));

        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByProductNameAsc(userId).stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        Map<UUID, String> images = productImageService.firstImageUrls(
                cartItems.stream().map(CartItem::getProduct).toList());

        Order order = new Order();
        order.setUser(user);
        long subtotal = 0;

        for (CartItem cartItem: cartItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            if (!product.isActive() || !product.getCategory().isActive()) {
                throw new BadRequestException(product.getName() + " is no longer available. Please remove it from your cart");
            }
            if (productRepository.reserveStock(product.getId(), quantity) == 0) {
                throw new BadRequestException("Not enough stock for " + product.getName() + ". Please update your cart");
            }

            long lineTotal = product.getPrice() * quantity;
            subtotal += lineTotal;

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setImageUrl(images.get(product.getId()));
            item.setUnitPrice(product.getPrice());
            item.setQuantity(quantity);
            item.setLineTotal(lineTotal);
            order.addItem(item);
        }

        Coupon coupon = null;
        long discount = 0;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponService.lockUsableCoupon(userId, request.getCouponCode(), subtotal);
            discount = couponService.calculateDiscount(coupon, subtotal);
        }

        long shippingFee = shippingFeeService.feeFor(subtotal - discount);
        long total = subtotal - discount + shippingFee;
        if (total < MIN_PAYABLE_PAISE) {
            throw new BadRequestException("This order is too small to pay for online");
        }

        order.setOrderNumber("BG" + orderRepository.nextOrderNumber());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(total);
        order.setCouponCode(coupon == null ? null : coupon.getCode());
        order.setExpiresAt(Instant.now().plus(Duration.ofMinutes(PAYMENT_WINDOW_MINUTES)));
        copyAddress(order, address);

        order = orderRepository.saveAndFlush(order);

        if (coupon != null) {
            couponService.recordRedemption(coupon, user, order.getId());
        }

        order.setRazorpayOrderId(razorpayGateway.createOrder(total, order.getOrderNumber()));
        orderRepository.save(order);

        return new CheckoutResponse(order.getId(), order.getOrderNumber(), subtotal, discount, shippingFee, total,
                "INR", appProperties.getRazorpay().getKeyId(), order.getRazorpayOrderId(), order.getExpiresAt());
    }

    private void copyAddress(Order order, Address address) {
        order.setShipName(address.getName());
        order.setShipPhone(address.getPhone());
        order.setShipLine1(address.getLine1());
        order.setShipLine2(address.getLine2());
        order.setShipCity(address.getCity());
        order.setShipState(address.getState());
        order.setShipPincode(address.getPincode());
    }
}
