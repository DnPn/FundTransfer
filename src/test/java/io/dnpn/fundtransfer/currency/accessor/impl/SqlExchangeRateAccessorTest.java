package io.dnpn.fundtransfer.currency.accessor.impl;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateException;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SqlExchangeRateAccessorTest {

    private static final Currency SOURCE_CURRENCY = Currency.EUR;
    private static final Currency TARGET_CURRENCY = Currency.AUD;
    private static final ExchangeRateRequest REQUEST = ExchangeRateRequest.builder()
            .fromCurrency(SOURCE_CURRENCY)
            .toCurrency(TARGET_CURRENCY)
            .build();
    private static final BigDecimal SOURCE_TO_USD_RATE = new BigDecimal("1.02");
    private static final BigDecimal TARGET_TO_USD_RATE = new BigDecimal("0.69");
    private static final BigDecimal EXPECTED_OVERALL_EXCHANGE_RATE = new BigDecimal("1.47");

    private static final String SOURCE_METHOD_INVALID_EXCHANGE_RATE = "provideInvalidExchangeRates";

    @Mock
    private JpaExchangeRateAccessor jpaAccessor;
    @InjectMocks
    private SqlExchangeRateAccessor sqlAccessor;

    private static Stream<Arguments> provideInvalidExchangeRates() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-2.5"))
        );
    }

    @Test
    void GIVEN_nullRequest_WHEN_getExchangeRate_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> sqlAccessor.getExchangeRate(null));
    }

    @Test
    void GIVEN_noSourceExchangeRate_WHEN_getExchangeRate_THEN_throwExchangeRate() {
        mockNoExchangeRate(SOURCE_CURRENCY);

        assertThrows(ExchangeRateException.class, () -> sqlAccessor.getExchangeRate(REQUEST));
    }

    @ParameterizedTest
    @MethodSource(SOURCE_METHOD_INVALID_EXCHANGE_RATE)
    void GIVEN_invalidSourceExchangeRate_WHEN_getExchangeRate_THEN_throwExchangeRate(BigDecimal invalidExchangeRate) {
        mockStoredExchangeRate(SOURCE_CURRENCY, invalidExchangeRate);

        assertThrows(ExchangeRateException.class, () -> sqlAccessor.getExchangeRate(REQUEST));
    }

    @Test
    void GIVEN_noTargetExchangeRate_WHEN_getExchangeRate_THEN_throwExchangeRate() {
        mockStoredExchangeRate(SOURCE_CURRENCY, SOURCE_TO_USD_RATE);
        mockNoExchangeRate(TARGET_CURRENCY);

        assertThrows(ExchangeRateException.class, () -> sqlAccessor.getExchangeRate(REQUEST));
    }

    @ParameterizedTest
    @MethodSource(SOURCE_METHOD_INVALID_EXCHANGE_RATE)
    void GIVEN_invalidTargetExchangeRate_WHEN_getExchangeRate_THEN_throwExchangeRate(BigDecimal invalidExchangeRate) {
        mockStoredExchangeRate(SOURCE_CURRENCY, SOURCE_TO_USD_RATE);
        mockStoredExchangeRate(TARGET_CURRENCY, invalidExchangeRate);

        assertThrows(ExchangeRateException.class, () -> sqlAccessor.getExchangeRate(REQUEST));
    }

    @SneakyThrows
    @Test
    void WHEN_getExchangeRate_THEN_returnOverallExchangeRate() {
        mockStoredExchangeRate(SOURCE_CURRENCY, SOURCE_TO_USD_RATE);
        mockStoredExchangeRate(TARGET_CURRENCY, TARGET_TO_USD_RATE);

        BigDecimal overallExchangeRate = sqlAccessor.getExchangeRate(REQUEST);

        BigDecimal diff = EXPECTED_OVERALL_EXCHANGE_RATE.subtract(overallExchangeRate).abs();
        // arbitrary error margin set for this test
        BigDecimal errorMargin = new BigDecimal("0.01");
        // we verify that the diff between the actual and expected exchange rate is less than the error margin
        assertTrue(diff.compareTo(errorMargin) < 0);
    }

    private void mockNoExchangeRate(Currency currency) {
        doReturn(Optional.empty()).when(jpaAccessor).findById(currency);
    }

    private void mockStoredExchangeRate(Currency currency, BigDecimal rateToUsd) {
        JpaExchangeRateEntity entity = JpaExchangeRateEntity.builder()
                .currency(currency)
                .rateToUsd(rateToUsd)
                .build();
        doReturn(Optional.of(entity)).when(jpaAccessor).findById(currency);
    }
}