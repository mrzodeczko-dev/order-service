package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.application.dto.OrderItemDto;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.dto.PlaceOrderResultDto;
import com.rzodeczko.application.service.order.OrderLifecycleService;
import com.rzodeczko.application.service.order.OrderQueryService;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.presentation.dto.mapper.CreateOrderDtoMapper;
import com.rzodeczko.presentation.dto.mapper.OrderItemDtoMapper;
import com.rzodeczko.presentation.dto.mapper.OrderSummaryDtoMapper;
import com.rzodeczko.presentation.dto.request.ConfirmPaymentRequestDto;
import com.rzodeczko.presentation.dto.request.MoveStoreRequestDto;
import com.rzodeczko.presentation.dto.request.PlaceOrderRequestDto;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@Import({CreateOrderDtoMapper.class, OrderSummaryDtoMapper.class, OrderItemDtoMapper.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean(name = "transactionalOrderLifecycleService")
    private OrderLifecycleService orderLifecycleService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @Test
    void shouldCreateDraftOrder() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderLifecycleService.createDraft(storeId)).thenReturn(new CreateOrderDto(orderId, "DRAFT"));

        // when / then
        mockMvc.perform(post("/orders").param("storeId", storeId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void shouldPlaceOrder() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderSummaryDto summary = new OrderSummaryDto(
                orderId, storeId, OrderStatus.AWAITING_PAYMENT,
                List.of(new OrderItemDto(productId, 1, "10.00 PLN", "10.00 PLN")),
                "10.00 PLN"
        );
        PlaceOrderResultDto result = new PlaceOrderResultDto(summary, paymentId, "http://pay");

        when(orderLifecycleService.placeOrder(eq(orderId), any(), any(), any())).thenReturn(result);

        PlaceOrderRequestDto request = new PlaceOrderRequestDto("buyer@test.com", "Buyer", "TAX123");

        // when / then
        mockMvc.perform(post("/orders/{orderId}/place", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.paymentRedirectUrl").value("http://pay"));
    }

    @Test
    void shouldReturnValidationErrorForInvalidPlaceOrderRequest() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        String invalidRequest = """
                {"buyerEmail": "not-an-email", "buyerName": "", "buyerTaxId": ""}
                """;

        // when / then
        mockMvc.perform(post("/orders/{orderId}/place", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldConfirmPayment() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        ConfirmPaymentRequestDto request = new ConfirmPaymentRequestDto(paymentId);

        // when / then
        mockMvc.perform(post("/orders/{orderId}/payment", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(orderLifecycleService).confirmPayment(orderId, paymentId);
    }

    @Test
    void shouldCancelOrder() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderSummaryDto summary = new OrderSummaryDto(orderId, storeId, OrderStatus.CANCELLED, List.of(), "0.00 PLN");
        when(orderLifecycleService.cancelOrder(orderId)).thenReturn(summary);

        // when / then
        mockMvc.perform(post("/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldFulfillOrder() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderSummaryDto summary = new OrderSummaryDto(orderId, storeId, OrderStatus.FULFILLED, List.of(), "10.00 PLN");
        when(orderLifecycleService.fulfillOrder(orderId)).thenReturn(summary);

        // when / then
        mockMvc.perform(post("/orders/{orderId}/fulfill", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FULFILLED"));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderSummaryDto summary = new OrderSummaryDto(orderId, storeId, OrderStatus.DRAFT, List.of(), "0.00 PLN");
        when(orderQueryService.getOrder(orderId)).thenReturn(summary);

        // when / then
        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        when(orderQueryService.getOrder(orderId)).thenThrow(new OrderNotFoundException(orderId));

        // when / then
        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldListOrders() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderSummaryDto summary = new OrderSummaryDto(orderId, storeId, OrderStatus.DRAFT, List.of(), "0.00 PLN");
        when(orderQueryService.listOrders()).thenReturn(List.of(summary));

        // when / then
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()));
    }

    @Test
    void shouldMoveOrderToAnotherStore() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID oldStoreId = UUID.randomUUID();
        UUID newStoreId = UUID.randomUUID();
        OrderSummaryDto summary = new OrderSummaryDto(orderId, newStoreId, OrderStatus.DRAFT, List.of(), "0.00 PLN");
        when(orderLifecycleService.moveOrderToAnotherStore(orderId, oldStoreId, newStoreId)).thenReturn(summary);

        MoveStoreRequestDto request = new MoveStoreRequestDto(oldStoreId, newStoreId);

        // when / then
        mockMvc.perform(post("/orders/{orderId}/move", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(newStoreId.toString()));
    }
}
