package io.dnpn.fundtransfer.currency.service.impl;

import io.dnpn.fundtransfer.common.annotation.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Currency conversion properties (used in the file `application.properties`).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyConversionProperty {

    /**
     * Prefix of all properties related to the currency conversion.
     */
    private static final String PROPERTY_PREFIX = "currencyConversion.";
    /**
     * Prefix of all properties related to the currency conversion via API.
     */
    private static final String API_PREFIX = PROPERTY_PREFIX + "api.";

    public static final String MODE = PROPERTY_PREFIX + "mode";
    public static final String SQL_MODE = "sql";
    public static final String API_MODE = "api";

    @VisibleForTesting
    static final String API_KEY = API_PREFIX + "key";
    @VisibleForTesting
    static final String API_BASE_URL = API_PREFIX + "baseUrl";
    @VisibleForTesting
    static final String API_REQUEST_TIMEOUT = API_PREFIX + "requestTimeoutMs";

    @VisibleForTesting
    static final Duration DEFAULT_API_REQUEST_TIMEOUT = Duration.ofSeconds(1);

    private final Environment environment;

    /**
     * Gets the API key for the currency conversion API.
     *
     * @return the API key.
     */
    public String getApiKey() {
        return environment.getProperty(CurrencyConversionProperty.API_KEY);
    }

    /**
     * Gets the base URL for the currency conversion API.
     *
     * @return the base URL.
     */
    public String getApiBaseUrl() {
        return environment.getProperty(CurrencyConversionProperty.API_BASE_URL);
    }

    /**
     * Gets the timeout duration for a request to the currency conversion API.
     *
     * @return the timeout duration.
     */
    public Duration getApiRequestTimeout() {
        final String propertyName = CurrencyConversionProperty.API_REQUEST_TIMEOUT;
        final String timeoutAsString = environment.getProperty(propertyName);
        return Optional.ofNullable(timeoutAsString)
                .map(value -> parseLong(value, propertyName))
                .map(Duration::ofMillis)
                .orElse(DEFAULT_API_REQUEST_TIMEOUT);
    }

    private long parseLong(String value, String propertyName) {
        try {
            return Long.parseLong(value);

        } catch (RuntimeException exception) {
            log.error("Invalid value for <{}>. Expected a long but got <{}>.", propertyName, value);
            throw exception;
        }
    }
}
