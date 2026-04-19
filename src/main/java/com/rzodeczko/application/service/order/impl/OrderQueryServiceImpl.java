package com.rzodeczko.application.service.order.impl;

import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.service.order.OrderQueryService;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;

import java.util.List;
import java.util.UUID;

public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    public OrderQueryServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderSummaryDto getOrder(UUID orderId) {
        return orderRepository
                .findById(new OrderId(orderId))
                .map(OrderSummaryDto::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    public List<OrderSummaryDto> listOrders() {
        return orderRepository
                .findAll()
                .stream()
                .map(OrderSummaryDto::from)
                .toList();
    }
}
