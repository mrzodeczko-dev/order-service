package com.rzodeczko.application.dto;

import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderSummaryDto(
        UUID orderId,
        UUID storeId,
        OrderStatus status,
        List<OrderItemDto> items,
        String totalAmount
) {
    public static OrderSummaryDto from(Order order) {
        return new OrderSummaryDto(
                order.getId().id(),
                order.getStoreId().id(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemDto::from).toList(),
                order.getTotalAmount().toString()
        );
    }
}
