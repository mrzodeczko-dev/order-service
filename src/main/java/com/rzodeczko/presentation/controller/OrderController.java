package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.service.order.OrderLifecycleService;
import com.rzodeczko.application.service.order.OrderQueryService;
import com.rzodeczko.presentation.dto.mapper.CreateOrderDtoMapper;
import com.rzodeczko.presentation.dto.mapper.OrderSummaryDtoMapper;
import com.rzodeczko.presentation.dto.request.ConfirmPaymentRequestDto;
import com.rzodeczko.presentation.dto.request.MoveStoreRequestDto;
import com.rzodeczko.presentation.dto.request.PlaceOrderRequestDto;
import com.rzodeczko.presentation.dto.response.CreateOrderResponseDto;
import com.rzodeczko.presentation.dto.response.OrderSummaryResponseDto;
import com.rzodeczko.presentation.dto.response.PlaceOrderResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderLifecycleService orderLifecycleService;
    private final OrderQueryService orderQueryService;
    private final CreateOrderDtoMapper createOrderDtoMapper;
    private final OrderSummaryDtoMapper orderSummaryDtoMapper;

    public OrderController(
            @Qualifier("transactionalOrderLifecycleService")
            OrderLifecycleService orderLifecycleService,
            OrderQueryService orderQueryService,
            CreateOrderDtoMapper createOrderDtoMapper,
            OrderSummaryDtoMapper orderSummaryDtoMapper) {
        this.orderLifecycleService = orderLifecycleService;
        this.orderQueryService = orderQueryService;
        this.createOrderDtoMapper = createOrderDtoMapper;
        this.orderSummaryDtoMapper = orderSummaryDtoMapper;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponseDto> createDraft(@RequestParam UUID storeId) {
        var result = orderLifecycleService.createDraft(storeId);
        return ResponseEntity
                .created(URI.create("/orders/" + result.orderId()))
                .body(createOrderDtoMapper.toResponse(result));
    }

    @PostMapping("/{orderId}/place")
    public ResponseEntity<PlaceOrderResponseDto> placeOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody PlaceOrderRequestDto request
    ) {
        var result = orderLifecycleService.placeOrder(
                orderId,
                request.buyerEmail(),
                request.buyerName(),
                request.buyerTaxId()
        );

        return ResponseEntity.ok(new PlaceOrderResponseDto(
                orderSummaryDtoMapper.toResponse(result.order()),
                result.paymentId(),
                result.paymentRedirectUrl()
        ));
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<Void> confirmPayment(
            @PathVariable UUID orderId,
            @Valid @RequestBody ConfirmPaymentRequestDto request
    ) {
        orderLifecycleService.confirmPayment(orderId, request.paymentId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderSummaryResponseDto> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderLifecycleService.cancelOrder(orderId)
        ));
    }

    @PostMapping("/{orderId}/fulfill")
    public ResponseEntity<OrderSummaryResponseDto> fulfillOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderLifecycleService.fulfillOrder(orderId)
        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderSummaryResponseDto> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderQueryService.getOrder(orderId)
        ));
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponseDto>> listOrders() {
        return ResponseEntity.ok(orderQueryService
                .listOrders()
                .stream()
                .map(orderSummaryDtoMapper::toResponse)
                .toList());
    }

    @PostMapping("/{orderId}/move")
    public ResponseEntity<OrderSummaryResponseDto> moveOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody MoveStoreRequestDto request
    ) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderLifecycleService.moveOrderToAnotherStore(
                        orderId, request.oldStoreId(), request.newStoreId())
        ));
    }
}
