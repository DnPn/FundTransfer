package io.dnpn.fundtransfer.currency.service;

import io.dnpn.fundtransfer.currency.accessor.ExchangeRateAccessor;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateException;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service to convert currencies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final ExchangeRateAccessor accessor;

    /**
     * Converts an amount from one currency to another.
     *
     * @param request the conversion request.
     * @return the converted amount.
     */
    public BigDecimal convert(@NonNull CurrencyConversionRequest request) throws ExchangeRateException {
        if (request.fromCurrency() == request.toCurrency()) {
            return request.amount();
        }
        final BigDecimal exchangeRate = getExchangeRate(request);
        final BigDecimal convertedAmount = request.amount().multiply(exchangeRate);
        log.debug("{} {} = {} {} (exchange rate: {})",
                request.amount(), request.fromCurrency(),
                convertedAmount, request.toCurrency(),
                exchangeRate);
        return convertedAmount;
    }

    private BigDecimal getExchangeRate(CurrencyConversionRequest request) throws ExchangeRateException {
        final ExchangeRateRequest exchangeRateRequest = ExchangeRateRequest.builder()
                .fromCurrency(request.fromCurrency())
                .toCurrency(request.toCurrency())
                .build();
        return accessor.getExchangeRate(exchangeRateRequest);
    }
}
