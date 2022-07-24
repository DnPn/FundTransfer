package io.dnpn.fundtransfer.currency.accessor;

import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Accessor to retrieve the exchange rate between 2 currencies.
 */
public interface ExchangeRateAccessor {

    BigDecimal getExchangeRate(@NonNull ExchangeRateRequest request) throws ExchangeRateException;
}
