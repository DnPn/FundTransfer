package io.dnpn.fundtransfer.currency.accessor;

import io.dnpn.fundtransfer.currency.Currency;
import lombok.Builder;
import lombok.NonNull;

/**
 * Request to get the exchange rate from one currency to the other.
 *
 * @param fromCurrency source currency.
 * @param toCurrency   target currency.
 */
@Builder
public
record ExchangeRateRequest(
        @NonNull Currency fromCurrency,
        @NonNull Currency toCurrency
) {
}
