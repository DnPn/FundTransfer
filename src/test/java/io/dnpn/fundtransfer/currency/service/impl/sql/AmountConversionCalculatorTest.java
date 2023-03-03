package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.common.MoneyHandling;
import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmountConversionCalculatorTest {

    private static final BigDecimal AMOUNT = new BigDecimal("12345.67");
    private static final ExchangeRateEntity SOURCE_RATE = ExchangeRateEntity.builder()
            .rateToUsd(new BigDecimal("1.23"))
            .currency(Currency.GBP)
            .build();
    private static final ExchangeRateEntity TARGET_RATE = ExchangeRateEntity.builder()
            .rateToUsd(new BigDecimal("0.0076"))
            .currency(Currency.JPY)
            .build();
    // calculated amount using the constants defined above: AMOUNT, SOURCE_RATE and TARGET_RATE
    private static final BigDecimal EXPECTED_CONVERTED_AMOUNT = new BigDecimal("1998049.22");

    private static final String METHOD_SOURCE_INVALID_RATE_TO_USD = "provideInvalidRateToUsd";

    private static final AmountConversionCalculator calculator = new AmountConversionCalculator();

    @Test
    void GIVEN_nullRequest_WHEN_convert_THEN_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> calculator.convert(null));
    }

    @ParameterizedTest
    @MethodSource(METHOD_SOURCE_INVALID_RATE_TO_USD)
    void GIVEN_invalidSourceRateToUsd_WHEN_convert_THEN_throwsCurrencyConversion(BigDecimal invalidRate) {
        ExchangeRateEntity sourceRate = ExchangeRateEntity.builder()
                .rateToUsd(invalidRate)
                .currency(SOURCE_RATE.getCurrency())
                .build();
        AmountConversionCalculatorRequest request = AmountConversionCalculatorRequest.builder()
                .amount(AMOUNT)
                .sourceExchangeRate(sourceRate)
                .targetExchangeRate(TARGET_RATE)
                .build();

        assertThrows(CurrencyConversionException.class, () -> calculator.convert(request));
    }

    @ParameterizedTest
    @MethodSource(METHOD_SOURCE_INVALID_RATE_TO_USD)
    void GIVEN_invalidTargetRateToUsd_WHEN_convert_THEN_throwsCurrencyConversion(BigDecimal invalidRate) {
        ExchangeRateEntity targetRate = ExchangeRateEntity.builder()
                .rateToUsd(invalidRate)
                .currency(TARGET_RATE.getCurrency())
                .build();
        AmountConversionCalculatorRequest request = AmountConversionCalculatorRequest.builder()
                .amount(AMOUNT)
                .sourceExchangeRate(SOURCE_RATE)
                .targetExchangeRate(targetRate)
                .build();

        assertThrows(CurrencyConversionException.class, () -> calculator.convert(request));
    }

    @SneakyThrows
    @Test
    void WHEN_convert_THEN_returnConvertedAmount() {
        AmountConversionCalculatorRequest request = AmountConversionCalculatorRequest.builder()
                .amount(AMOUNT)
                .sourceExchangeRate(SOURCE_RATE)
                .targetExchangeRate(TARGET_RATE)
                .build();

        BigDecimal actualConvertedAmount = calculator.convert(request)
                .setScale(MoneyHandling.SCALE_FOR_MONEY, MoneyHandling.ROUNDING_MODE_FOR_CLIENT_CREDIT);

        assertEquals(EXPECTED_CONVERTED_AMOUNT, actualConvertedAmount);
    }

    private static Stream<Arguments> provideInvalidRateToUsd() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-1.23"))
        );
    }
}