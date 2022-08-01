package io.dnpn.fundtransfer.currency.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.Environment;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class CurrencyConversionPropertyTest {

    private static final String SOME_PROPERTY_VALUE = "something";
    private static final long VALID_TIMEOUT_VALUE = 1234;
    private static final String INVALID_TIMEOUT_VALUE = "abc";

    @Mock
    private Environment environment;
    @InjectMocks
    private CurrencyConversionProperty accessor;

    @Test
    void WHEN_getApiKey_THEN_returnPropertyValue() {
        doReturn(SOME_PROPERTY_VALUE)
                .when(environment)
                .getProperty(CurrencyConversionProperty.API_KEY);

        var actualValue = accessor.getApiKey();

        assertEquals(SOME_PROPERTY_VALUE, actualValue);
    }

    @Test
    void WHEN_getBaseUrl_THEN_returnPropertyValue() {
        doReturn(SOME_PROPERTY_VALUE)
                .when(environment)
                .getProperty(CurrencyConversionProperty.API_BASE_URL);

        var actualValue = accessor.getApiBaseUrl();

        assertEquals(SOME_PROPERTY_VALUE, actualValue);
    }

    @Test
    void GIVEN_propertyNotSet_WHEN_getApiRequestTimeout_THEN_returnDefaultValue() {
        doReturn(null)
                .when(environment)
                .getProperty(CurrencyConversionProperty.API_REQUEST_TIMEOUT);

        var actualTimeout = accessor.getApiRequestTimeout();

        assertEquals(CurrencyConversionProperty.DEFAULT_API_REQUEST_TIMEOUT, actualTimeout);
    }

    @Test
    void WHEN_getApiRequestTimeout_THEN_returnValueInMillis() {
        doReturn(String.valueOf(VALID_TIMEOUT_VALUE))
                .when(environment)
                .getProperty(CurrencyConversionProperty.API_REQUEST_TIMEOUT);

        var actualTimeout = accessor.getApiRequestTimeout();

        var expectedTimeout = Duration.ofMillis(VALID_TIMEOUT_VALUE);
        assertEquals(expectedTimeout, actualTimeout);
    }

    @Test
    void GIVEN_invalidProperty_WHEN_getRequestTimeout_THEN_logErrorDetails(CapturedOutput capturedOutput) {
        doReturn(INVALID_TIMEOUT_VALUE)
                .when(environment)
                .getProperty(CurrencyConversionProperty.API_REQUEST_TIMEOUT);

        try {
            accessor.getApiRequestTimeout();
            fail("An exception should have been thrown.");

        } catch (NumberFormatException exception) {
            var log = capturedOutput.getOut();
            // log the name of the problematic property
            assertTrue(log.contains(CurrencyConversionProperty.API_REQUEST_TIMEOUT));
            // log the value of the problematic property
            assertTrue(log.contains(INVALID_TIMEOUT_VALUE));
        }
    }
}