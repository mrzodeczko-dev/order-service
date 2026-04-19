package com.rzodeczko.presentation.dto.mapper;

import com.rzodeczko.application.dto.OrderItemDto;
import com.rzodeczko.presentation.dto.response.OrderItemResponseDto;
import org.springframework.stereotype.Component;

@Component
public class OrderItemDtoMapper {
    public OrderItemResponseDto toResponse(OrderItemDto dto) {
        return new OrderItemResponseDto(
                dto.productId(),
                dto.quantity(),
                dto.unitPrice(),
                dto.lineTotal()
        );
    }
}
