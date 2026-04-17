package com.rzodeczko.application.command.order;

import java.util.UUID;

public record RemoveItemFromOrderCommand(
        UUID orderId,
        UUID productId
) {
}
