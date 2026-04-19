package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.service.order.OrderItemService;
import com.rzodeczko.presentation.dto.mapper.OrderSummaryDtoMapper;
import com.rzodeczko.presentation.dto.request.AddItemRequestDto;
import com.rzodeczko.presentation.dto.request.ReplaceItemRequestDto;
import com.rzodeczko.presentation.dto.response.OrderSummaryResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders/{orderId}/items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemService orderItemService;
    private final OrderSummaryDtoMapper orderSummaryDtoMapper;

    @PostMapping
    public ResponseEntity<OrderSummaryResponseDto> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddItemRequestDto request
    ) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderItemService.addItem(
                        orderId,
                        request.productId(),
                        request.quantity(),
                        request.unitPrice()
                )
        ));
    }

    @DeleteMapping
    public ResponseEntity<OrderSummaryResponseDto> removeItem(
            @PathVariable UUID orderId,
            @RequestParam UUID productId
    ) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderItemService.removeItem(orderId, productId)
        ));
    }

    @PostMapping("/replace")
    public ResponseEntity<OrderSummaryResponseDto> replaceProduct(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReplaceItemRequestDto request
    ) {
        return ResponseEntity.ok(orderSummaryDtoMapper.toResponse(
                orderItemService.replaceProductInOrder(
                        orderId,
                        request.oldProductId(),
                        request.newProductId(),
                        request.newQuantity(),
                        request.newUnitPrice(),
                        request.storeId()
                )
        ));
    }
}
