package io.dnpn.fundtransfer.currency.service.impl.api;

import io.dnpn.fundtransfer.common.annotation.VisibleForTesting;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Helper used to handle errors thrown by the {@link org.springframework.web.reactive.function.client.WebClient}
 * from {@link ApiCurrencyConversionService} and to wrap them in a {@link CurrencyConversionException}.
 */
@Slf4j
@Component
@ConditionalOnBean(ApiCurrencyConversionService.class)
class ApiCurrencyConversionErrorHandler {

    private static final String UNAUTHORIZED_DETAILS_MESSAGE = "No valid API key provided. Please verify the API key " +
            "set in `application.properties`.";
    private static final String TOO_MANY_REQUESTS_DETAILS_MESSAGE = "API request limit exceeded. Either: subscribe to" +
            " a higher tier of the API, use a different API key or use a different implementation of the currency " +
            "conversion service.";

    @VisibleForTesting
    static final Map<HttpStatus, String> STATUS_DETAILS = Map.ofEntries(
            Map.entry(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_DETAILS_MESSAGE),
            Map.entry(HttpStatus.TOO_MANY_REQUESTS, TOO_MANY_REQUESTS_DETAILS_MESSAGE)
    );

    /**
     * Handles an exception throw by a {@link org.springframework.web.reactive.function.client.WebClient} and wraps
     * it in a {@link io.dnpn.fundtransfer.currency.service.CurrencyConversionException}.
     *
     * @param request   the request causing the exception.
     * @param exception the exception to handle.
     * @return the wrapping exception.
     */
    public CurrencyConversionException handleAndWrap(CurrencyConversionRequest request, RuntimeException exception) {
        if (exception instanceof WebClientResponseException webClientResponseException) {
            logExceptionDetails(webClientResponseException);
        }

        final var message = String.format("Failed to convert the amount %f from %s to %s.",
                request.amount(), request.fromCurrency(), request.toCurrency());
        return new CurrencyConversionException(message, exception);
    }

    private void logExceptionDetails(WebClientResponseException exception) {
        final var statusCode = exception.getStatusCode();
        final var errorMessage = exception.getResponseBodyAsString();

        log.error("Currency exchange API call failed with the statusCode code {}: {}", statusCode, errorMessage);

        if (STATUS_DETAILS.containsKey(statusCode)) {
            log.error(STATUS_DETAILS.get(statusCode));
        }
    }
}
