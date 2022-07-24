package io.dnpn.fundtransfer.currency.service;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateAccessor;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("123.45");

    @Mock
    private ExchangeRateAccessor accessor;
    @InjectMocks
    private CurrencyConversionService service;

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

        verify(accessor, never()).getExchangeRate(any());

    }

    @SneakyThrows
    @Test
    void GIVEN_differentCurrencies_WHEN_convert_THEN_returnConvertedAmount() {
        Currency sourceCurrency = Currency.EUR;
        Currency targetCurrency = Currency.USD;
        CurrencyConversionRequest conversionRequest = CurrencyConversionRequest.builder()
                .amount(AMOUNT)
                .fromCurrency(sourceCurrency)
                .toCurrency(targetCurrency)
                .build();

        ExchangeRateRequest exchangeRateRequest = ExchangeRateRequest.builder()
                .fromCurrency(sourceCurrency)
                .toCurrency(targetCurrency)
                .build();
        BigDecimal conversionRate = new BigDecimal("78.90");
        doReturn(conversionRate).when(accessor).getExchangeRate(exchangeRateRequest);

        BigDecimal convertedAmount = service.convert(conversionRequest);

        BigDecimal expectedConvertedAmount = AMOUNT.multiply(conversionRate);
        assertEquals(expectedConvertedAmount, convertedAmount);
    }


    private CurrencyConversionRequest buildRequestWithSameCurrency(BigDecimal amount) {
        Currency currency = Currency.EUR;
        return CurrencyConversionRequest.builder()
                .amount(amount)
                .toCurrency(currency)
                .fromCurrency(currency)
                .build();
    }
}