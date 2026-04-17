package com.rzodeczko.domain.model.product;

import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.ProductId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Product.
 */
class ProductTest {

    @Test
    void shouldCreateProduct() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("99.99"), Currency.getInstance("PLN"));

        // when
        Product product = new Product(productId, "LAPTOP-001", "Gaming Laptop", unitPrice, true);

        // then
        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getSku()).isEqualTo("LAPTOP-001");
        assertThat(product.getName()).isEqualTo("Gaming Laptop");
        assertThat(product.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenProductIdIsNull() {
        // given
        Money unitPrice = new Money(new BigDecimal("99.99"), Currency.getInstance("PLN"));

        // when & then
        assertThatThrownBy(() -> new Product(null, "LAPTOP-001", "Gaming Laptop", unitPrice, true))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product id cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenSkuIsNull() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("99.99"), Currency.getInstance("PLN"));

        // when & then
        assertThatThrownBy(() -> new Product(productId, null, "Gaming Laptop", unitPrice, true))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Product sku cannot be null");
    }

    @Test
    void shouldDeactivateProduct() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("99.99"), Currency.getInstance("PLN"));
        Product product = new Product(productId, "LAPTOP-001", "Gaming Laptop", unitPrice, true);

        // when
        product.deactivate();

        // then
        assertThat(product.isActive()).isFalse();
    }

    @Test
    void shouldActivateProduct() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("99.99"), Currency.getInstance("PLN"));
        Product product = new Product(productId, "LAPTOP-001", "Gaming Laptop", unitPrice, false);

        // when
        product.activate();

        // then
        assertThat(product.isActive()).isTrue();
    }
}

