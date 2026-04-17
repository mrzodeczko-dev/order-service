package com.rzodeczko.application.dto;

import com.rzodeczko.domain.model.order.Order;

import java.util.UUID;

public record CreateOrderDto(UUID orderId, String status) {
    public static CreateOrderDto from(Order order) {
        return new CreateOrderDto(
                order.getId().id(),
                order.getStatus().name()
        );
    }
}
