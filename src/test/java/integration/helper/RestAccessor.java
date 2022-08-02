package integration.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class RestAccessor {

    private static final Duration REQUEST_TIMEOUT = Duration.ofMillis(500);
    private static final String BASE_URL = "http://localhost:%d";

    private final WebClient webClient;

    private Optional<ResponseEntity> latestResponse = Optional.empty();

    public RestAccessor(@LocalServerPort int port) {
        final var baseUrl = String.format(BASE_URL, port);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void ping() {
        log.debug("Send ping");
        final var response = webClient.get()
                .uri(builder -> builder.path("ping").build())
                .retrieve()
                .toBodilessEntity()
                .block(REQUEST_TIMEOUT);
        saveResponse(response);
    }

    private void saveResponse(ResponseEntity response) {
        latestResponse = Optional.of(response);
        log.debug("Latest response: {}", response);
    }

    public ResponseEntity getLatestResponse() {
        return latestResponse.orElseThrow(
                () -> new NoSuchElementException("You make an API call before retrieving the response"));
    }
}
