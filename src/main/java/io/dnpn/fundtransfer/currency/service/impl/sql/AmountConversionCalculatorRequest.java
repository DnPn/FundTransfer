package io.dnpn.fundtransfer.currency.service.impl.sql;

import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Request to convert an amount with the given exchange rate.
 *
 * @param amount             the amount to convert.
 * @param sourceExchangeRate the exchange rate of the source currency.
 * @param targetExchangeRate the exchange rate of the target currency.
 */
@Builder
record AmountConversionCalculatorRequest(
        @NonNull BigDecimal amount,
        @NonNull JpaExchangeRateEntity sourceExchangeRate,
        @NonNull JpaExchangeRateEntity targetExchangeRate
) {
}
