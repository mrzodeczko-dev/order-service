package com.rzodeczko.presentation.dto.response;

import com.rzodeczko.domain.model.order.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderSummaryResponseDto(
        UUID orderId,
        UUID storeId,
        OrderStatus status,
        List<OrderItemResponseDto> items,
        String totalAmount
) {
}
