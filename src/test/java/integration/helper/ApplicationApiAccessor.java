package integration.helper;

import io.dnpn.fundtransfer.transfer.api.TransferApiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class ApplicationApiAccessor {

    private static final Duration REQUEST_TIMEOUT = Duration.ofMillis(1000);
    private static final String BASE_URL = "http://localhost:%d";

    private static final String PING_PATH = "ping";
    private static final String TRANSFER_PATH = "transfer";

    private final WebClient webClient;

    private Optional<ResponseEntity> latestResponse = Optional.empty();

    public ApplicationApiAccessor(@LocalServerPort int port) {
        final var baseUrl = String.format(BASE_URL, port);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void ping() {
        log.debug("Send ping");
        final var request = webClient.get()
                .uri(builder -> builder.path(PING_PATH).build());
        callAndSaveResponse(request);
    }

    public void transfer(TransferApiRequest requestBody) {
        log.debug("Sending transfer request: {}", requestBody);
        final var request = webClient.post()
                .uri(builder -> builder.path(TRANSFER_PATH).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody);
        callAndSaveResponse(request);
    }

    private <T> void callAndSaveResponse(WebClient.RequestHeadersSpec request) {
        final var response = request.retrieve()
                .toEntity(Object.class)
                .onErrorResume(this::exceptionToResponse)
                .block(REQUEST_TIMEOUT);

        log.debug("Response: {}", response);
        latestResponse = Optional.of(response);
    }

    private Mono<ResponseEntity<Object>> exceptionToResponse(Throwable throwable) {
        // in the case of a WebClientResponseException we can extract the response
        if (throwable instanceof WebClientResponseException exception) {
            return Mono.just(toResponseEntity(exception));

        } else {
            return Mono.error(throwable);
        }
    }

    private ResponseEntity toResponseEntity(WebClientResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .location(exception.getHeaders().getLocation())
                .contentType(exception.getHeaders().getContentType())
                .contentLength(exception.getHeaders().getContentLength())
                .headers(exception.getHeaders())
                .body(exception.getResponseBodyAsString());
    }

    public ResponseEntity getLatestResponse() {
        return latestResponse.orElseThrow(
                () -> new NoSuchElementException("You make an API call before retrieving the response"));
    }
}
