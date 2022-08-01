package io.dnpn.fundtransfer.currency.service.impl.api;

import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionService;
import io.dnpn.fundtransfer.currency.service.impl.CurrencyConversionProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Implementation of {@link CurrencyConversionService} using the `exchangerates` API (see https://exchangeratesapi.io/).
 */
@Slf4j
@Service
@ConditionalOnProperty(
        name = CurrencyConversionProperty.MODE,
        havingValue = CurrencyConversionProperty.API_MODE
)
public class ApiCurrencyConversionService implements CurrencyConversionService {

    private static final String API_KEY_HEADER = "apiKey";
    private static final String REMAINING_MONTHLY_CALLS_HEADER = "x-ratelimit-remaining-month";
    private static final String REMAINING_DAILY_CALLS_HEADER = "x-ratelimit-remaining-day";

    private static final String FROM_QUERY_PARAM = "from";
    private static final String TO_QUERY_PARAM = "to";
    private static final String AMOUNT_QUERY_PARAM = "amount";

    private final WebClient webClient;
    private final Duration requestTimeout;
    private final ApiCurrencyConversionErrorHandler errorHandler;

    public ApiCurrencyConversionService(CurrencyConversionProperty currencyConversionProperty,
                                        ApiCurrencyConversionErrorHandler errorHandler) {
        final String apiKey = currencyConversionProperty.getApiKey();
        final String baseUrl = currencyConversionProperty.getApiBaseUrl();
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(API_KEY_HEADER, apiKey)
                .build();

        this.errorHandler = errorHandler;
        this.requestTimeout = currencyConversionProperty.getApiRequestTimeout();
    }

    @Override
    public BigDecimal convert(@NonNull CurrencyConversionRequest request) throws CurrencyConversionException {
        try {
            final var response = callCurrencyConversionApi(request);
            return handleResponse(request, response);

        } catch (RuntimeException exception) {
            throw errorHandler.handleAndWrap(request, exception);
        }
    }

    private ResponseEntity<ApiCurrencyConversionSuccessResponse> callCurrencyConversionApi(CurrencyConversionRequest request) {
        final var queryParams = getQueryParam(request);
        return this.webClient.get()
                .uri(builder -> builder.queryParams(queryParams).build())
                .retrieve()
                .toEntity(ApiCurrencyConversionSuccessResponse.class)
                .block(requestTimeout);
    }

    private MultiValueMap<String, String> getQueryParam(CurrencyConversionRequest request) {
        final var queryParams = new LinkedMultiValueMap<String, String>();
        queryParams.add(FROM_QUERY_PARAM, request.fromCurrency().toString());
        queryParams.add(TO_QUERY_PARAM, request.toCurrency().toString());
        queryParams.add(AMOUNT_QUERY_PARAM, request.amount().toPlainString());
        return queryParams;
    }

    private BigDecimal handleResponse(CurrencyConversionRequest request, ResponseEntity<ApiCurrencyConversionSuccessResponse> response) {
        logRemainingApiCalls(response);

        final var convertedAmount = response.getBody().result();

        log.info("API successfully converted {} {} to {} {}",
                request.amount(), request.fromCurrency(),
                convertedAmount, request.toCurrency());

        return convertedAmount;
    }

    private void logRemainingApiCalls(ResponseEntity<ApiCurrencyConversionSuccessResponse> response) {
        log.info("Currency conversion API remaining calls: [this month: {}; today: {}]",
                response.getHeaders().getFirst(REMAINING_MONTHLY_CALLS_HEADER),
                response.getHeaders().getFirst(REMAINING_DAILY_CALLS_HEADER));
    }
}
