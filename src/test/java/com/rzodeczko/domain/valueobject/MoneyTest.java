package com.rzodeczko.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Money value object.
 */
class MoneyTest {

    @Test
    void shouldCreateMoneyWithValidAmount() {
        // given
        BigDecimal amount = new BigDecimal("10.50");
        Currency currency = Currency.getInstance("PLN");

        // when
        Money money = new Money(amount, currency);

        // then
        assertThat(money.amount()).isEqualByComparingTo(amount);
        assertThat(money.currency()).isEqualTo(currency);
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        // given
        Currency currency = Currency.getInstance("PLN");

        // when & then
        assertThatThrownBy(() -> new Money(null, currency))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money amount and currency cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNull() {
        // given
        BigDecimal amount = new BigDecimal("10.50");

        // when & then
        assertThatThrownBy(() -> new Money(amount, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money amount and currency cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        // given
        BigDecimal amount = new BigDecimal("-10.50");
        Currency currency = Currency.getInstance("PLN");

        // when & then
        assertThatThrownBy(() -> new Money(amount, currency))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money amount cannot be negative");
    }

    @Test
    void shouldRoundAmountToTwoDecimals() {
        // given
        BigDecimal amount = new BigDecimal("10.567");
        Currency currency = Currency.getInstance("PLN");

        // when
        Money money = new Money(amount, currency);

        // then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.57"));
    }

    @Test
    void shouldAddMoney() {
        // given
        Money money1 = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Money money2 = new Money(new BigDecimal("20.30"), Currency.getInstance("PLN"));

        // when
        Money result = money1.add(money2);

        // then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("30.80"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance("PLN"));
    }

    @Test
    void shouldThrowExceptionWhenAddingMoneyWithDifferentCurrency() {
        // given
        Money money1 = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Money money2 = new Money(new BigDecimal("20.30"), Currency.getInstance("USD"));

        // when & then
        assertThatThrownBy(() -> money1.add(money2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currencies do not match");
    }

    @Test
    void shouldMultiplyMoney() {
        // given
        Money money = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));

        // when
        Money result = money.multiply(3);

        // then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("31.50"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance("PLN"));
    }

    @Test
    void shouldVerifyZeroPLN() {
        // when
        Money zeroPln = Money.ZERO_PLN;

        // then
        assertThat(zeroPln.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(zeroPln.currency()).isEqualTo(Currency.getInstance("PLN"));
    }

    @Test
    void shouldBeEqualForSameAmountAndCurrency() {
        // given
        Money money1 = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Money money2 = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));

        // when & then
        assertThat(money1).isEqualTo(money2);
    }

    @Test
    void shouldNotBeEqualForDifferentAmounts() {
        // given
        Money money1 = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Money money2 = new Money(new BigDecimal("20.50"), Currency.getInstance("PLN"));

        // when & then
        assertThat(money1).isNotEqualTo(money2);
    }
}

