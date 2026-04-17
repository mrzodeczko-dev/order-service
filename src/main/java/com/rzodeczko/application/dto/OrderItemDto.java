package com.rzodeczko.application.dto;


import com.rzodeczko.domain.model.order.OrderItem;

import java.util.UUID;

public record OrderItemDto(UUID productId, int quantity, String unitPrice, String lineTotal) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(
                item.getProductId().id(),
                item.getQuantity(),
                item.getUnitPrice().toString(),
                item.lineTotal().toString()
        );
    }
}
