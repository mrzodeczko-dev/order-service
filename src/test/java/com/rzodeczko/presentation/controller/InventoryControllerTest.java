package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.service.inventory.InventoryService;
import com.rzodeczko.domain.exception.InsufficientStockException;
import com.rzodeczko.domain.exception.InventoryNotFoundException;
import com.rzodeczko.presentation.dto.request.ReleaseStockRequestDto;
import com.rzodeczko.presentation.dto.request.ReplenishStockRequestDto;
import com.rzodeczko.presentation.dto.request.ReserveStockRequestDto;
import com.rzodeczko.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void shouldReserveStock() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReserveStockRequestDto request = new ReserveStockRequestDto(storeId, productId, 5);

        // when / then
        mockMvc.perform(post("/inventories/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inventoryService).reserve(storeId, productId, 5);
    }

    @Test
    void shouldReleaseStock() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReleaseStockRequestDto request = new ReleaseStockRequestDto(storeId, productId, 3);

        // when / then
        mockMvc.perform(post("/inventories/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inventoryService).release(storeId, productId, 3);
    }

    @Test
    void shouldReplenishStock() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReplenishStockRequestDto request = new ReplenishStockRequestDto(storeId, productId, 10);

        // when / then
        mockMvc.perform(post("/inventories/replenish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inventoryService).replenish(storeId, productId, 10);
    }

    @Test
    void shouldReturn409WhenInsufficientStock() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReserveStockRequestDto request = new ReserveStockRequestDto(storeId, productId, 100);

        doThrow(new InsufficientStockException(productId, 100, 5))
                .when(inventoryService).reserve(storeId, productId, 100);

        // when / then
        mockMvc.perform(post("/inventories/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturn404WhenInventoryNotFound() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReserveStockRequestDto request = new ReserveStockRequestDto(storeId, productId, 1);

        doThrow(new InventoryNotFoundException(storeId, productId))
                .when(inventoryService).reserve(storeId, productId, 1);

        // when / then
        mockMvc.perform(post("/inventories/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnValidationErrorWhenQuantityIsZero() throws Exception {
        // given
        String invalidRequest = """
                {"storeId": "%s", "productId": "%s", "quantity": 0}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        // when / then
        mockMvc.perform(post("/inventories/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
