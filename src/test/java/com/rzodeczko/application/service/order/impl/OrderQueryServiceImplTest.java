package com.rzodeczko.application.service.order.impl;

import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderQueryServiceImplTest {

    private OrderQueryServiceImpl service;
    private OrderRepository orderRepository;

    private static final Currency PLN = Currency.getInstance("PLN");

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        service = new OrderQueryServiceImpl(orderRepository);
    }

    @Test
    void shouldReturnOrderById() {
        OrderId orderId = OrderId.newId();
        Order order = new Order(orderId, StoreId.newId());
        order.addItem(new OrderItem(ProductId.newId(), 1, new Money(BigDecimal.TEN, PLN)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderSummaryDto result = service.getOrder(orderId.id());

        assertThat(result.orderId()).isEqualTo(orderId.id());
        assertThat(result.status()).isEqualTo(OrderStatus.DRAFT);
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldListAllOrders() {
        Order order1 = new Order(OrderId.newId(), StoreId.newId());
        Order order2 = new Order(OrderId.newId(), StoreId.newId());

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        List<OrderSummaryDto> result = service.listOrders();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderSummaryDto> result = service.listOrders();

        assertThat(result).isEmpty();
    }
}
