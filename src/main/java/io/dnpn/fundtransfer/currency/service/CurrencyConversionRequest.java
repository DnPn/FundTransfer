package io.dnpn.fundtransfer.currency.service;

import io.dnpn.fundtransfer.currency.Currency;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Request to convert an amount from one currency to another.
 *
 * @param fromCurrency source currency.
 * @param toCurrency   target currency.
 * @param amount       the amount to convert.
 */
@Builder
public record CurrencyConversionRequest(
        @NonNull Currency fromCurrency,
        @NonNull Currency toCurrency,
        @NonNull BigDecimal amount
) {
}
