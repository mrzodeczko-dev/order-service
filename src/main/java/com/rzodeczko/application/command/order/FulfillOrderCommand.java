package com.rzodeczko.application.command.order;

import java.util.UUID;

public record FulfillOrderCommand(UUID orderId) {
}
