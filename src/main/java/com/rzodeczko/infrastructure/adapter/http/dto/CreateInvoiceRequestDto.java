package com.rzodeczko.infrastructure.adapter.http.dto;

import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequestDto(UUID orderId, String taxId, String buyerName, List<ItemRequestDto> items) {
}
