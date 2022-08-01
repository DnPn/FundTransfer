package io.dnpn.fundtransfer.currency.service.impl.api;

import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import io.dnpn.fundtransfer.currency.service.impl.CurrencyConversionProperty;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ApiCurrencyConversionServiceTest {
    private static final String API_KEY_HEADER = "apiKey";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String AMOUNT_QUERY_PARAM = "amount";
    private static final String FROM_QUERY_PARAM = "from";
    private static final String TO_QUERY_PARAM = "to";

    private static final Duration TIMEOUT = Duration.ofMillis(500);
    private static final String API_KEY = "abcd-1234";

    private static final CurrencyConversionRequest REQUEST = CurrencyConversionRequest.builder()
            .amount(new BigDecimal(25))
            .fromCurrency(Currency.GBP)
            .toCurrency(Currency.JPY)
            .build();
    private static final String SUCCESS_RESPONSE_BODY = """
            {
              "date": "2018-02-22",
              "historical": "",
              "info": {
                "rate": 148.972231,
                "timestamp": 1519328414
              },
              "query": {
                "amount": 25,
                "from": "GBP",
                "to": "JPY"
              },
              "result": 3724.305775,
              "success": true
            }
            """;
    private static final BigDecimal EXPECTED_RESULT = new BigDecimal("3724.305775");

    private static MockWebServer webServer;
    private static String baseUrl;


    @Mock
    private CurrencyConversionProperty currencyConversionProperty;
    @Mock
    private ApiCurrencyConversionErrorHandler errorHandler;

    private ApiCurrencyConversionService service;

    @BeforeAll
    static void beforeAll() throws IOException {
        webServer = new MockWebServer();
        webServer.start();

        baseUrl = String.format("http://localhost:%s/convert", webServer.getPort());
    }

    @BeforeEach
    void beforeEach() {
        doReturn(baseUrl).when(currencyConversionProperty).getApiBaseUrl();
        doReturn(TIMEOUT).when(currencyConversionProperty).getApiRequestTimeout();
        doReturn(API_KEY).when(currencyConversionProperty).getApiKey();

        this.service = new ApiCurrencyConversionService(currencyConversionProperty, errorHandler);
    }

    @AfterAll
    static void afterAll() throws IOException {
        webServer.shutdown();
    }

    @Test
    void GIVEN_nullRequest_WHEN_convert_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> service.convert(null));
    }

    @Test
    void GIVEN_timeout_WHEN_convert_THEN_throwCurrencyConversion() {
        mockErrorHandler();

        var moreThanTimeout = TIMEOUT.multipliedBy(2);
        var response = createSuccessfulResponse()
                .setBodyDelay(moreThanTimeout.toMillis(), TimeUnit.MILLISECONDS);
        webServer.enqueue(response);

        assertThrows(CurrencyConversionException.class, () -> service.convert(REQUEST));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 429, 500})
    void GIVEN_errorStatusCode_WHEN_convert_THEN_throwCurrencyConversion(int statusCode) {
        mockErrorHandler();

        var response = new MockResponse().setResponseCode(statusCode);
        webServer.enqueue(response);

        assertThrows(CurrencyConversionException.class, () -> service.convert(REQUEST));
    }

    @Test
    void WHEN_convert_THEN_getRequestToTheBaseUrl() {
        webServer.enqueue(createSuccessfulResponse());

        var capturedRequest = callServiceAndCaptureRequest();

        assertEquals(HttpMethod.GET.toString(), capturedRequest.getMethod());

        var requestUrl = capturedRequest.getRequestUrl().url();
        assertEquals(baseUrl, getBaseUrl(requestUrl));
    }

    @Test
    void WHEN_convert_THEN_requestValuesAreInQueryParams() {
        webServer.enqueue(createSuccessfulResponse());

        var capturedRequest = callServiceAndCaptureRequest();

        var requestUrl = capturedRequest.getRequestUrl();
        assertEquals(REQUEST.amount().toPlainString(), requestUrl.queryParameter(AMOUNT_QUERY_PARAM));
        assertEquals(REQUEST.fromCurrency().toString(), requestUrl.queryParameter(FROM_QUERY_PARAM));
        assertEquals(REQUEST.toCurrency().toString(), requestUrl.queryParameter(TO_QUERY_PARAM));
    }

    @Test
    void WHEN_convert_THEN_apiKeyHeaderIsPresent() {
        webServer.enqueue(createSuccessfulResponse());

        var capturedRequest = callServiceAndCaptureRequest();

        assertEquals(API_KEY, capturedRequest.getHeader(API_KEY_HEADER));
    }

    @Test
    @SneakyThrows
    void WHEN_convert_THEN_returnResult() {
        webServer.enqueue(createSuccessfulResponse());

        BigDecimal result = service.convert(REQUEST);

        assertEquals(EXPECTED_RESULT, result);
    }

    private void mockErrorHandler() {
        doReturn(new CurrencyConversionException("Some error."))
                .when(errorHandler)
                .handleAndWrap(any(), any());
    }

    private MockResponse createSuccessfulResponse() {
        return new MockResponse()
                .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .setBody(SUCCESS_RESPONSE_BODY)
                .setResponseCode(HttpStatus.OK.value());
    }

    @SneakyThrows
    private RecordedRequest callServiceAndCaptureRequest() {
        service.convert(REQUEST);
        return webServer.takeRequest();
    }

    private String getBaseUrl(URL url) {
        return url.toString().split("\\?")[0];
    }
}