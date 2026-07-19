package com.rzodeczko.presentation.dto.mapper;

import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.presentation.dto.response.CreateOrderResponseDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderDtoMapperTest {

    private final CreateOrderDtoMapper mapper = new CreateOrderDtoMapper();

    @Test
    void shouldMapToResponse() {
        // given
        UUID orderId = UUID.randomUUID();
        CreateOrderDto dto = new CreateOrderDto(orderId, "DRAFT");

        // when
        CreateOrderResponseDto response = mapper.toResponse(dto);

        // then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo("DRAFT");
    }
}
