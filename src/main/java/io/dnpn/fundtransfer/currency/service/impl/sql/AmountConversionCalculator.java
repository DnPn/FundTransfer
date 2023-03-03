package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Helper class for the {@link SqlCurrencyConversionService} to convert an amount using the exchange rate information
 * retrieved from the database.
 */
@Slf4j
@Component
@ConditionalOnBean(SqlCurrencyConversionService.class)
class AmountConversionCalculator {

    private static final int DIVISION_PRECISION = 10;
    private static final MathContext DIVISION_MATH_CONTEXT = new MathContext(DIVISION_PRECISION);

    BigDecimal convert(@NonNull AmountConversionCalculatorRequest request) throws CurrencyConversionException {
        final BigDecimal exchangeRate = getExchangeRate(request);
        final BigDecimal convertedAmount = request.amount().multiply(exchangeRate);

        log.debug("{} {} = {} {} (exchange rate: {})",
                request.amount(), request.sourceExchangeRate().getCurrency(),
                convertedAmount, request.targetExchangeRate().getCurrency(),
                exchangeRate);
        return convertedAmount;
    }

    private BigDecimal getExchangeRate(AmountConversionCalculatorRequest request) throws CurrencyConversionException {
        BigDecimal sourceCurrencyRateToUsd = validateAndGetRateToUsd(request.sourceExchangeRate());
        BigDecimal targetCurrencyRateToUsd = validateAndGetRateToUsd(request.targetExchangeRate());
        return sourceCurrencyRateToUsd.divide(targetCurrencyRateToUsd, DIVISION_MATH_CONTEXT);
    }

    private BigDecimal validateAndGetRateToUsd(ExchangeRateEntity exchangeRate) throws CurrencyConversionException {
        final BigDecimal rateToUsd = exchangeRate.getRateToUsd();

        if (rateToUsd.compareTo(BigDecimal.ZERO) <= 0) {
            final String message = String.format("Invalid exchange rate to USD for the currency %s: %.2f",
                    exchangeRate.getCurrency(), rateToUsd);
            throw new CurrencyConversionException(message);
        }
        return rateToUsd;
    }
}
