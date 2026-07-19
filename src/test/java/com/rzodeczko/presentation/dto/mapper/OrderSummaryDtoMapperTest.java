package com.rzodeczko.presentation.dto.mapper;

import com.rzodeczko.application.dto.OrderItemDto;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.presentation.dto.response.OrderSummaryResponseDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSummaryDtoMapperTest {

    private final OrderItemDtoMapper itemMapper = new OrderItemDtoMapper();
    private final OrderSummaryDtoMapper mapper = new OrderSummaryDtoMapper(itemMapper);

    @Test
    void shouldMapToResponseWithItems() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderItemDto item = new OrderItemDto(productId, 2, "10.00 PLN", "20.00 PLN");
        OrderSummaryDto dto = new OrderSummaryDto(orderId, storeId, OrderStatus.DRAFT, List.of(item), "20.00 PLN");

        // when
        OrderSummaryResponseDto response = mapper.toResponse(dto);

        // then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.storeId()).isEqualTo(storeId);
        assertThat(response.status()).isEqualTo(OrderStatus.DRAFT);
        assertThat(response.totalAmount()).isEqualTo("20.00 PLN");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productId()).isEqualTo(productId);
    }

    @Test
    void shouldMapToResponseWithEmptyItems() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderSummaryDto dto = new OrderSummaryDto(orderId, storeId, OrderStatus.DRAFT, List.of(), "0.00 PLN");

        // when
        OrderSummaryResponseDto response = mapper.toResponse(dto);

        // then
        assertThat(response.items()).isEmpty();
    }
}
