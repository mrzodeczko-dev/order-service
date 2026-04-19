package com.rzodeczko.domain.exception;

import com.rzodeczko.domain.valueobject.ProductId;

public class OrderItemNotFoundException extends RuntimeException {
    public OrderItemNotFoundException(ProductId productId) {
        super("Item with product id " + productId.id() + " not found in order");
    }
}
