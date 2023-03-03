package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqlCurrencyConversionServiceTest {
    private static final BigDecimal AMOUNT = new BigDecimal("123.45");
    private static final Currency SOURCE_CURRENCY = Currency.GBP;
    private static final Currency TARGET_CURRENCY = Currency.JPY;
    private static final CurrencyConversionRequest REQUEST = CurrencyConversionRequest.builder()
            .fromCurrency(SOURCE_CURRENCY)
            .toCurrency(TARGET_CURRENCY)
            .amount(AMOUNT)
            .build();
    private static final ExchangeRateEntity SOURCE_RATE = ExchangeRateEntity.builder()
            .rateToUsd(new BigDecimal("111"))
            .currency(SOURCE_CURRENCY)
            .build();
    private static final ExchangeRateEntity TARGET_RATE = ExchangeRateEntity.builder()
            .rateToUsd(new BigDecimal("222"))
            .currency(TARGET_CURRENCY)
            .build();
    private static final BigDecimal CONVERTED_AMOUNT = new BigDecimal("456.78");

    @Mock
    private ExchangeRateRepository accessor;
    @Mock
    private AmountConversionCalculator calculator;
    @InjectMocks
    private SqlCurrencyConversionService service;

    @Test
    void GIVEN_nullRequest_WHEN_convert_THEN_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> service.convert(null));
    }

    @SneakyThrows
    @Test
    void GIVEN_sameCurrency_WHEN_convert_THEN_returnSameAmount() {
        CurrencyConversionRequest request = buildRequestWithSameCurrency(AMOUNT);

        BigDecimal convertedAmount = service.convert(request);

        assertEquals(AMOUNT, convertedAmount);
    }

    @SneakyThrows
    @Test
    void GIVEN_sameCurrency_WHEN_convert_THEN_doNotCallTheAccessor() {
        CurrencyConversionRequest request = buildRequestWithSameCurrency(AMOUNT);

        service.convert(request);

        verify(accessor, never()).findById(any());

    }

    @Test
    void GIVEN_sourceExchangeRateNotFound_WHEN_convert_THEN_throwsCurrencyConversion() {
        doReturn(Optional.empty()).when(accessor).findById(SOURCE_CURRENCY);

        assertThrows(CurrencyConversionException.class, () -> service.convert(REQUEST));
    }

    @Test
    void GIVEN_targetExchangeRateNotFound_WHEN_convert_THEN_throwsCurrencyConversion() {
        mockCurrencyRate(SOURCE_CURRENCY, SOURCE_RATE);
        doReturn(Optional.empty()).when(accessor).findById(TARGET_CURRENCY);

        assertThrows(CurrencyConversionException.class, () -> service.convert(REQUEST));
    }

    @SneakyThrows
    @Test
    void WHEN_convert_THEN_returnConvertedAmount() {
        mockCurrencyRate(SOURCE_CURRENCY, SOURCE_RATE);
        mockCurrencyRate(TARGET_CURRENCY, TARGET_RATE);

        AmountConversionCalculatorRequest calculatorRequest = AmountConversionCalculatorRequest.builder()
                .sourceExchangeRate(SOURCE_RATE)
                .targetExchangeRate(TARGET_RATE)
                .amount(AMOUNT)
                .build();
        doReturn(CONVERTED_AMOUNT).when(calculator).convert(calculatorRequest);

        BigDecimal actualConvertedAmount = service.convert(REQUEST);

        assertEquals(CONVERTED_AMOUNT, actualConvertedAmount);
    }

    private CurrencyConversionRequest buildRequestWithSameCurrency(BigDecimal amount) {
        Currency currency = Currency.GBP;
        return CurrencyConversionRequest.builder()
                .amount(amount)
                .toCurrency(currency)
                .fromCurrency(currency)
                .build();
    }

    private void mockCurrencyRate(Currency currency, ExchangeRateEntity jpaEntity) {
        doReturn(Optional.of(jpaEntity)).when(accessor).findById(currency);
    }
}