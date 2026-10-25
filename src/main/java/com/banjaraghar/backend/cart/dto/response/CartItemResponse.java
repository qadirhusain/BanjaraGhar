package com.banjaraghar.backend.cart.dto.response;

import com.banjaraghar.backend.catalog.product.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private UUID productId;
    private String name;
    private String slug;
    private String imageUrl;        // first image, null if the product has none
    private long price;             // paise, per unit
    private long mrp;               // paise, per unit
    private int quantity;
    private long lineTotal;         // paise, price x quantity
    private StockStatus stockStatus;
    private boolean available;      // false if hidden, or stock is below the quantity in the cart
}