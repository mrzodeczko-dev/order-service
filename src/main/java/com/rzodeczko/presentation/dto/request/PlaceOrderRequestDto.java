package com.rzodeczko.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PlaceOrderRequestDto(
        @NotBlank @Email(message = "buyerEmail must be valid") String buyerEmail,
        @NotBlank String buyerName,
        @NotBlank String buyerTaxId
) {
}
