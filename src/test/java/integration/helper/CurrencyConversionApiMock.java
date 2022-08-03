package integration.helper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class CurrencyConversionApiMock {

    private static final String PORT_PROPERTY = "mockWebServer.port";

    private final int port;
    private final MockWebServer webServer;

    public CurrencyConversionApiMock(Environment environment) {
        this.port = getPortFromProperties(environment);
        this.webServer = new MockWebServer();
    }

    private static int getPortFromProperties(Environment environment) {
        final var port = environment.getProperty(PORT_PROPERTY);
        return Integer.valueOf(port);
    }

    @PostConstruct
    private void postConstruct() throws IOException {
        this.webServer.start(port);
    }

    public void mockNextResponse(MockResponse response) {
        this.webServer.enqueue(response);
    }

    @PreDestroy
    private void preDestroy() throws IOException {
        if (this.webServer != null) {
            this.webServer.shutdown();
        }
    }
}
