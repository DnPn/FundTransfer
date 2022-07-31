package io.dnpn.fundtransfer.currency.service;

import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Service to convert currencies.
 */
public interface CurrencyConversionService {

    /**
     * Converts an amount from one currency to another.
     *
     * @param request the conversion request.
     * @return the converted amount.
     */
    BigDecimal convert(@NonNull CurrencyConversionRequest request) throws CurrencyConversionException;
}
