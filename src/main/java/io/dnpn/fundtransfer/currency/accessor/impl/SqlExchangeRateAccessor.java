package io.dnpn.fundtransfer.currency.accessor.impl;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateAccessor;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateException;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.MathContext;

@Repository
@RequiredArgsConstructor
public class SqlExchangeRateAccessor implements ExchangeRateAccessor {

    private static final int DIVISION_PRECISION = 10;
    private static final MathContext DIVISION_MATH_CONTEXT = new MathContext(DIVISION_PRECISION);

    private final JpaExchangeRateAccessor jpaAccessor;

    @Override
    public BigDecimal getExchangeRate(@NonNull ExchangeRateRequest request) throws ExchangeRateException {
        final BigDecimal fromCurrencyRateToUsd = getRateToUsd(request.fromCurrency());
        assertValidRateToUsd(request.fromCurrency(), fromCurrencyRateToUsd);

        final BigDecimal toCurrencyRateToUsd = getRateToUsd(request.toCurrency());
        assertValidRateToUsd(request.toCurrency(), toCurrencyRateToUsd);

        return fromCurrencyRateToUsd.divide(toCurrencyRateToUsd, DIVISION_MATH_CONTEXT);
    }

    private BigDecimal getRateToUsd(Currency currency) throws ExchangeRateException {
        return jpaAccessor.findById(currency)
                .map(JpaExchangeRateEntity::getRateToUsd)
                .orElseThrow(() -> supplyCurrencyNotFoundException(currency));
    }

    private ExchangeRateException supplyCurrencyNotFoundException(Currency currency) {
        return new ExchangeRateException("No exchange rate to USD registered for the currency " + currency);
    }

    private void assertValidRateToUsd(Currency currency, BigDecimal rateToUsd) throws ExchangeRateException {
        if (rateToUsd.compareTo(BigDecimal.ZERO) <= 0) {
            final String message = String.format("Invalid exchange rate to USD for the currency %s: %.2f", currency,
                    rateToUsd);
            throw new ExchangeRateException(message);
        }
    }
}
