package com.rzodeczko.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing monetary amounts with currency.
 */
public class Money {
    /** The monetary amount. */
    private final BigDecimal amount;
    /** The currency. */
    private final Currency currency;

    /** Zero amount in PLN. */
    public static final Money ZERO_PLN
            = new Money(BigDecimal.ZERO, Currency.getInstance("PLN"));

    /**
     * Creates a new Money instance.
     * @param amount the amount
     * @param currency the currency
     * @throws IllegalArgumentException if amount or currency is null, or amount is negative
     */
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Money amount and currency cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative");
        }

        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    /**
     * Gets the amount.
     * @return the amount
     */
    public BigDecimal amount() {
        return amount;
    }

    /**
     * Gets the currency.
     * @return the currency
     */
    public Currency currency() {
        return currency;
    }

    /**
     * Adds another Money to this one.
     * @param other the other Money
     * @return the sum
     * @throws IllegalArgumentException if currencies do not match
     */
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /**
     * Multiplies this Money by an integer factor.
     * @param factor the factor
     * @return the multiplied Money
     */
    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    /**
     * Ensures the currencies are the same.
     * @param other the other Money
     * @throws IllegalArgumentException if currencies do not match
     */
    private void ensureSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies do not match");
        }
    }

    @Override
    public String toString() {
        return amount + " " + currency.getCurrencyCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
