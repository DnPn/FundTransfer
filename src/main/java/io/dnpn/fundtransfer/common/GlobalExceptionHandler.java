package io.dnpn.fundtransfer.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler to send user-friendly error messages.
 * <p>
 * As much as possible exceptions should be captured and wrapped in a {@link ResponseStatusException} with a
 * user-friendly reason.
 * <p>
 * Exceptions that failed to be captured as a {@link ResponseStatusException} will result in a 500 error sent to the
 * client asking them to retry, and if it still fails to contact the support with the request ID set by the
 * {@link MdcFilter}. The reason of the exception won't be visible in the error message sent to the client (to
 * prevent internal details from leaking), but it can be retrieved from the logs using the request ID.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String UNEXPECTED_EXCEPTION_CLIENT_MESSAGE = "Unexpected exception, please retry. If the " +
            "operation keeps failing please contact the support and provide them the following request ID: %s";
    private static final String UNEXPECTED_EXCEPTION_SERVER_MESSAGE = "Unexpected exception with request ID: %s";
    private static final String INVALID_HTTP_REQUEST_MESSAGE = "Invalid request, please verify that the format of the" +
            " request complies with the API documentation.";

    @ExceptionHandler({HttpMessageConversionException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity httpMessageConversionExceptionHandler() {
        return new ResponseEntity(INVALID_HTTP_REQUEST_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity responseStatusExceptionHandler(ResponseStatusException exception) {
        return new ResponseEntity(exception.getReason(), exception.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity defaultExceptionHandler(Exception exception) {
        final String requestId = MDC.get(MdcFilter.REQUEST_ID_MDC_FIELD);

        final String serverMessage = String.format(UNEXPECTED_EXCEPTION_SERVER_MESSAGE, requestId);
        log.error(serverMessage, exception);

        final String clientMessage = String.format(UNEXPECTED_EXCEPTION_CLIENT_MESSAGE, requestId);
        return new ResponseEntity(clientMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
