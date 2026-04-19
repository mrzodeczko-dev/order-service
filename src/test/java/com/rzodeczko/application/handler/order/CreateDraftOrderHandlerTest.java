package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.order.CreateDraftOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateDraftOrderHandler.
 */
class CreateDraftOrderHandlerTest {

    private CreateDraftOrderHandler handler;
    private OrderRepository orderRepository;
    private StoreId storeId;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        handler = new CreateDraftOrderHandler(orderRepository);
        storeId = StoreId.newId();
    }

    @Test
    void shouldCreateDraftOrder() {
        // given
        CreateDraftOrderCommand command = new CreateDraftOrderCommand(storeId.id());

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isEqualTo(storeId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(result.getItems()).isEmpty();
        verify(orderRepository, times(1)).save(result);
    }

    @Test
    void shouldReturnOrderWithGeneratedId() {
        // given
        CreateDraftOrderCommand command = new CreateDraftOrderCommand(storeId.id());

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getId().id()).isNotNull();
    }

    @Test
    void shouldCreateDraftOrderWithoutItems() {
        // given
        CreateDraftOrderCommand command = new CreateDraftOrderCommand(storeId.id());

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    void shouldSaveDraftOrderToRepository() {
        // given
        CreateDraftOrderCommand command = new CreateDraftOrderCommand(storeId.id());

        // when
        Order result = handler.handle(command);

        // then
        verify(orderRepository, times(1)).save(result);
    }
}

