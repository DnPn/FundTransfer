package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmountConversionCalculatorTest {

    private static final BigDecimal AMOUNT = new BigDecimal("12345.67");
    private static final JpaExchangeRateEntity SOURCE_RATE = JpaExchangeRateEntity.builder()
            .rateToUsd(new BigDecimal("1.02"))
            .currency(Currency.EUR)
            .build();
    private static final JpaExchangeRateEntity TARGET_RATE = JpaExchangeRateEntity.builder()
            .rateToUsd(new BigDecimal("0.69"))
            .currency(Currency.AUD)
            .build();
    // calculated amount using the constants defined above: AMOUNT, SOURCE_RATE and TARGET_RATE
    private static final BigDecimal EXPECTED_CONVERTED_AMOUNT = new BigDecimal("18250.12");

    private static final String METHOD_SOURCE_INVALID_RATE_TO_USD = "provideInvalidRateToUsd";

    private static final AmountConversionCalculator calculator = new AmountConversionCalculator();

    @Test
    void GIVEN_nullRequest_WHEN_convert_THEN_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> calculator.convert(null));
    }

    @ParameterizedTest
    @MethodSource(METHOD_SOURCE_INVALID_RATE_TO_USD)
    void GIVEN_invalidSourceRateToUsd_WHEN_convert_THEN_throwsCurrencyConversion(BigDecimal invalidRate) {
        JpaExchangeRateEntity sourceRate = JpaExchangeRateEntity.builder()
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
        JpaExchangeRateEntity targetRate = JpaExchangeRateEntity.builder()
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

        BigDecimal actualConvertedAmount = calculator.convert(request);

        // truncates the converted amount to match the number of decimals of EXPECTED_CONVERTED_AMOUNT
        BigDecimal actualConvertedAmountSameScale = actualConvertedAmount.setScale(
                EXPECTED_CONVERTED_AMOUNT.scale(),
                RoundingMode.DOWN);
        assertEquals(EXPECTED_CONVERTED_AMOUNT, actualConvertedAmountSameScale);
    }

    private static Stream<Arguments> provideInvalidRateToUsd() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(new BigDecimal("-1.23"))
        );
    }
}