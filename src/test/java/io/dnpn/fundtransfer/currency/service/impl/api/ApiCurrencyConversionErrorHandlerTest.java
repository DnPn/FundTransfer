package io.dnpn.fundtransfer.currency.service.impl.api;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class ApiCurrencyConversionErrorHandlerTest {

    private static final String ERRORS_WITH_DETAILS_MESSAGE_METHOD_SOURCE = "errorsWithDetailMessageProvider";
    private static final CurrencyConversionRequest REQUEST = CurrencyConversionRequest.builder()
            .fromCurrency(Currency.EUR)
            .toCurrency(Currency.CNY)
            .amount(BigDecimal.ONE)
            .build();

    private ApiCurrencyConversionErrorHandler handler;

    @BeforeEach
    void beforeEach() {
        this.handler = new ApiCurrencyConversionErrorHandler();
    }

    @Test
    void WHEN_handleAndWrap_THEN_wrapInCurrencyConversionException() {
        var initialException = new RuntimeException();

        var wrappingException = this.handler.handleAndWrap(REQUEST, initialException);

        assertEquals(initialException, wrappingException.getCause());
    }

    @Test
    void GIVEN_webClientResponseException_WHEN_handleAndWrap_THEN_logStatusAndMessage(CapturedOutput capturedOutput) {
        var status = HttpStatus.I_AM_A_TEAPOT;
        var errorMessage = "Cannot brew coffee";
        var exception = new WebClientResponseException(status.value(), status.getReasonPhrase(), null,
                errorMessage.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        handler.handleAndWrap(REQUEST, exception);

        var statusCode = String.valueOf(status.value());
        var logs = capturedOutput.getOut();
        assertTrue(logs.contains(statusCode));
        assertTrue(logs.contains(errorMessage));
    }

    @ParameterizedTest
    @MethodSource(ERRORS_WITH_DETAILS_MESSAGE_METHOD_SOURCE)
    void GIVEN_unauthorised_WHEN_handleAndWrap_THEN_logAdditionalDetails(HttpStatus status, String message,
                                                                         CapturedOutput capturedOutput) {
        var exception = new WebClientResponseException(status.value(), status.getReasonPhrase(), null, null, null);

        handler.handleAndWrap(REQUEST, exception);

        var logs = capturedOutput.getOut();
        assertTrue(logs.contains(message));
    }

    private static Stream<Arguments> errorsWithDetailMessageProvider() {
        return ApiCurrencyConversionErrorHandler.STATUS_DETAILS
                .entrySet()
                .stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }
}