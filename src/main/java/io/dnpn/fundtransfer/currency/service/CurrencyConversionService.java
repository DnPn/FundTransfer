package io.dnpn.fundtransfer.currency.service;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service to convert currencies.
 */
@Service
public class CurrencyConversionService {

    /**
     * Converts an amount from one currency to another.
     *
     * @param request the conversion request.
     * @return the converted amount.
     */
    public BigDecimal convert(@NonNull CurrencyConversionRequest request) {
        // TODO implement
        return BigDecimal.ONE;
    }
}
