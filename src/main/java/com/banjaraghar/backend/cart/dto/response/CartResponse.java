package com.banjaraghar.backend.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private List<CartItemResponse> items;
    private int totalQuantity;      // all items, for the cart badge
    private long subtotal;          // paise, available items only
}