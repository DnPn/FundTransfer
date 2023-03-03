package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionService;
import io.dnpn.fundtransfer.currency.service.impl.CurrencyConversionProperty;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = CurrencyConversionProperty.MODE,
        havingValue = CurrencyConversionProperty.SQL_MODE
)
public class SqlCurrencyConversionService implements CurrencyConversionService {

    private final ExchangeRateRepository repository;
    private final AmountConversionCalculator calculator;

    @Override
    public BigDecimal convert(@NonNull CurrencyConversionRequest request) throws CurrencyConversionException {
        if (request.fromCurrency() == request.toCurrency()) {
            return request.amount();
        }
        final ExchangeRateEntity sourceExchangeRate = getExchangeRate(request.fromCurrency());
        final ExchangeRateEntity targetExchangeRate = getExchangeRate(request.toCurrency());

        final AmountConversionCalculatorRequest conversionRequest = AmountConversionCalculatorRequest.builder()
                .sourceExchangeRate(sourceExchangeRate)
                .targetExchangeRate(targetExchangeRate)
                .amount(request.amount())
                .build();
        return calculator.convert(conversionRequest);
    }

    private ExchangeRateEntity getExchangeRate(Currency currency) throws CurrencyConversionException {
        return repository.findById(currency)
                .orElseThrow(() -> supplyCurrencyNotFoundException(currency));
    }

    private CurrencyConversionException supplyCurrencyNotFoundException(Currency currency) {
        final String message = String.format("No exchange rate to USD registered for the currency %s", currency);
        return new CurrencyConversionException(message);
    }
}
