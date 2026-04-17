package com.rzodeczko.application.port.data;

import java.math.BigDecimal;

public record InvoiceItemData(String name, int quantity, BigDecimal price) {
}
