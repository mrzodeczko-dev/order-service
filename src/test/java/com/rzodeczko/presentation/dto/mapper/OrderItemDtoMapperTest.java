package com.rzodeczko.presentation.dto.mapper;

import com.rzodeczko.application.dto.OrderItemDto;
import com.rzodeczko.presentation.dto.response.OrderItemResponseDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemDtoMapperTest {

    private final OrderItemDtoMapper mapper = new OrderItemDtoMapper();

    @Test
    void shouldMapToResponse() {
        // given
        UUID productId = UUID.randomUUID();
        OrderItemDto dto = new OrderItemDto(productId, 3, "19.99 PLN", "59.97 PLN");

        // when
        OrderItemResponseDto response = mapper.toResponse(dto);

        // then
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.unitPrice()).isEqualTo("19.99 PLN");
        assertThat(response.lineTotal()).isEqualTo("59.97 PLN");
    }
}
