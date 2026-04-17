package com.rzodeczko.application.service.order;

import com.rzodeczko.application.dto.OrderSummaryDto;

import java.util.List;
import java.util.UUID;

public interface OrderQueryService {
    OrderSummaryDto getOrder(UUID orderId);
    List<OrderSummaryDto> listOrders();
}
