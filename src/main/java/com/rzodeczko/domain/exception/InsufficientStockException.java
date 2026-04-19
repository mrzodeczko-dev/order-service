package com.rzodeczko.domain.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int requested, int available) {
        super("Not enough stock for product %s. Requested: %d, available: %d".formatted(
                productId, requested, available
        ));
    }
}
