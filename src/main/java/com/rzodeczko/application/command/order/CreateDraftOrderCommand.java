package com.rzodeczko.application.command.order;

import java.util.UUID;

public record CreateDraftOrderCommand(UUID storeId) {
}
