package com.rzodeczko.application.service.order.impl;

import com.rzodeczko.application.command.order.AddItemToOrderCommand;
import com.rzodeczko.application.command.order.RemoveItemFromOrderCommand;
import com.rzodeczko.application.command.order.ReplaceProductInOrderCommand;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.handler.order.AddItemToOrderHandler;
import com.rzodeczko.application.handler.order.RemoveItemFromOrderHandler;
import com.rzodeczko.application.handler.order.ReplaceProductInOrderHandler;
import com.rzodeczko.application.service.order.OrderItemService;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemServiceImpl implements OrderItemService {

    private final AddItemToOrderHandler addItemHandler;
    private final RemoveItemFromOrderHandler removeItemHandler;
    private final ReplaceProductInOrderHandler replaceProductHandler;

    public OrderItemServiceImpl(
            AddItemToOrderHandler addItemHandler,
            RemoveItemFromOrderHandler removeItemHandler,
            ReplaceProductInOrderHandler replaceProductHandler) {
        this.addItemHandler = addItemHandler;
        this.removeItemHandler = removeItemHandler;
        this.replaceProductHandler = replaceProductHandler;
    }

    @Override
    public OrderSummaryDto addItem(UUID orderId, UUID productId, int quantity, BigDecimal unitPrice) {
        return OrderSummaryDto.from(addItemHandler.handle(
                new AddItemToOrderCommand(orderId, productId, quantity, unitPrice)
        ));
    }

    @Override
    public OrderSummaryDto removeItem(UUID orderId, UUID productId) {
        return OrderSummaryDto.from(removeItemHandler.handle(
                new RemoveItemFromOrderCommand(orderId, productId)
        ));
    }

    @Override
    public OrderSummaryDto replaceProductInOrder(
            UUID orderId,
            UUID oldProductId,
            UUID newProductId,
            int newQuantity,
            BigDecimal newUnitPrice,
            UUID storeId) {
        return OrderSummaryDto.from(replaceProductHandler.handle(
                new ReplaceProductInOrderCommand(
                        orderId,
                        oldProductId,
                        newProductId,
                        newQuantity,
                        newUnitPrice,
                        storeId
                )
        ));
    }
}
