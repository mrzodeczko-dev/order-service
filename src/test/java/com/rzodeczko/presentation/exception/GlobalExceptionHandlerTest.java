package com.rzodeczko.presentation.exception;

import com.rzodeczko.domain.exception.*;
import com.rzodeczko.presentation.dto.error.ErrorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturn404ForOrderNotFoundException() {
        // given
        OrderNotFoundException ex = new OrderNotFoundException(UUID.randomUUID());

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle((RuntimeException) ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
    }

    @Test
    void shouldReturn404ForProductNotFoundException() {
        // given
        ProductNotFoundException ex = new ProductNotFoundException(UUID.randomUUID());

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle((RuntimeException) ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn404ForInventoryNotFoundException() {
        // given
        InventoryNotFoundException ex = new InventoryNotFoundException(UUID.randomUUID(), UUID.randomUUID());

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle((RuntimeException) ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn409ForInsufficientStockException() {
        // given
        InsufficientStockException ex = new InsufficientStockException(UUID.randomUUID(), 10, 5);

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
    }

    @Test
    void shouldReturn409ForInvalidOrderStateException() {
        // given
        InvalidOrderStateException ex = new InvalidOrderStateException("Cannot cancel");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn409ForOptimisticLockingFailure() {
        // given
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException("Order", "id-1");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("modified by another request");
    }

    @Test
    void shouldReturn400ForIllegalArgumentException() {
        // given
        IllegalArgumentException ex = new IllegalArgumentException("bad input");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("bad input");
    }

    @Test
    void shouldReturn409ForIllegalStateException() {
        // given
        IllegalStateException ex = new IllegalStateException("wrong state");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn502ForIllegalStateExceptionWithUnavailable() {
        // given
        IllegalStateException ex = new IllegalStateException("service unavailable");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().status()).isEqualTo(502);
    }

    @Test
    void shouldReturn500ForGenericException() {
        // given
        Exception ex = new Exception("unexpected");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
    }

    @Test
    void shouldReturn404ForNoResourceFoundException() throws Exception {
        // given
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown", "/unknown");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handle(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
