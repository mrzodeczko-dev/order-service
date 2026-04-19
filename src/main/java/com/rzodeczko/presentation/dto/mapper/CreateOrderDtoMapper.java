package com.rzodeczko.presentation.dto.mapper;

import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.presentation.dto.response.CreateOrderResponseDto;
import org.springframework.stereotype.Component;

@Component
public class CreateOrderDtoMapper {
    public CreateOrderResponseDto toResponse(CreateOrderDto dto) {
        return new CreateOrderResponseDto(dto.orderId(), dto.status());
    }
}
