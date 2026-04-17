package com.rzodeczko.application.service.order;

import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.dto.PlaceOrderResultDto;

import java.util.UUID;

public interface OrderLifecycleService {
    CreateOrderDto createDraft(UUID storeId);
    PlaceOrderResultDto placeOrder(UUID orderId, String buyerEmail, String buyerName, String buyerTaxId);
    void confirmPayment(UUID orderId, UUID paymentId);
    OrderSummaryDto fulfillOrder(UUID orderId);
    OrderSummaryDto cancelOrder(UUID orderId);
    OrderSummaryDto moveOrderToAnotherStore(UUID orderId, UUID oldStoreId, UUID newStoreId);
}
