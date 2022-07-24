package io.dnpn.fundtransfer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckControllerTest {

    private static HealthCheckController controller;

    @BeforeAll
    static void beforeAll() {
        controller = new HealthCheckController();
    }

    @Test
    void WHEN_ping_THEN_alwaysReturnNoContent() {
        final ResponseEntity response = controller.ping();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}