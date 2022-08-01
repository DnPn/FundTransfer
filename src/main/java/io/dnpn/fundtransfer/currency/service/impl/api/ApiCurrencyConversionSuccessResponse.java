package io.dnpn.fundtransfer.currency.service.impl.api;

import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Simplified version of the response returned by the `/convert` API (see
 * https://apilayer.com/marketplace/exchangerates_data-api#documentation-tab)
 *
 * @param result the converted amount.
 */
public record ApiCurrencyConversionSuccessResponse(
        @NonNull BigDecimal result
) {
}
