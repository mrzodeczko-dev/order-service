package com.rzodeczko.presentation.dto.mapper;

import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.presentation.dto.response.OrderSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSummaryDtoMapper {
    private final OrderItemDtoMapper orderItemDtoMapper;

    public OrderSummaryResponseDto toResponse(OrderSummaryDto dto) {
        return new OrderSummaryResponseDto(
                dto.orderId(),
                dto.storeId(),
                dto.status(),
                dto.items().stream().map(orderItemDtoMapper::toResponse).toList(),
                dto.totalAmount()
        );
    }
}
