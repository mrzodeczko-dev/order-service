package com.rzodeczko.application.service.order;

import com.rzodeczko.application.dto.OrderSummaryDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderItemService {
    OrderSummaryDto addItem(UUID orderId, UUID productId, int quantity, BigDecimal unitPrice);
    OrderSummaryDto removeItem(UUID orderId, UUID productId);
    OrderSummaryDto replaceProductInOrder(
            UUID orderId,
            UUID oldProductId,
            UUID newProductId,
            int newQuantity,
            BigDecimal newUnitPrice,
            UUID storeId
    );
}
