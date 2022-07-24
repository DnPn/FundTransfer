package io.dnpn.fundtransfer.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private static GlobalExceptionHandler handler;

    @BeforeAll
    static void beforeAll() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void WHEN_httpMessageConversionExceptionHandler_THEN_respondBadRequest() {
        ResponseEntity response = handler.httpMessageConversionExceptionHandler();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void WHEN_responseStatusExceptionHandler_THEN_respondWithSameStatusAndReason() {
        HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
        String reason = "Something went wrong!";
        ResponseStatusException exception = new ResponseStatusException(status, reason);

        ResponseEntity response = handler.responseStatusExceptionHandler(exception);

        assertEquals(status, response.getStatusCode());
        assertEquals(reason, response.getBody());
    }

    @Test
    void WHEN_defaultExceptionHandler_THEN_respondServerError() {
        Exception exception = new Exception();

        ResponseEntity response = handler.defaultExceptionHandler(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void WHEN_defaultExceptionHandler_THEN_writeInTheBodyTheRequestId() {
        Exception exception = new Exception();
        String requestId = "abcd-1234-defg-6789";
        MDC.put(MdcFilter.REQUEST_ID_MDC_FIELD, requestId);

        ResponseEntity<String> response = handler.defaultExceptionHandler(exception);

        assertTrue(response.getBody().contains(requestId));
    }
}