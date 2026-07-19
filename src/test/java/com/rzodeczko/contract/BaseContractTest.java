package com.rzodeczko.contract;

import com.rzodeczko.application.service.order.OrderLifecycleService;
import com.rzodeczko.application.service.order.OrderQueryService;
import com.rzodeczko.presentation.controller.OrderController;
import com.rzodeczko.presentation.dto.mapper.CreateOrderDtoMapper;
import com.rzodeczko.presentation.dto.mapper.OrderSummaryDtoMapper;
import com.rzodeczko.presentation.exception.GlobalExceptionHandler;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;


public abstract class BaseContractTest {

    private final OrderLifecycleService orderLifecycleService = mock(OrderLifecycleService.class);
    private final OrderQueryService orderQueryService = mock(OrderQueryService.class);
    private final CreateOrderDtoMapper createOrderDtoMapper = mock(CreateOrderDtoMapper.class);
    private final OrderSummaryDtoMapper orderSummaryDtoMapper = mock(OrderSummaryDtoMapper.class);

    @BeforeEach
    void setup() {
        doNothing().when(orderLifecycleService).confirmPayment(any(UUID.class), any(UUID.class));

        RestAssuredMockMvc.standaloneSetup(
                new OrderController(
                        orderLifecycleService,
                        orderQueryService,
                        createOrderDtoMapper,
                        orderSummaryDtoMapper
                ),
                new GlobalExceptionHandler()
        );
    }
}
