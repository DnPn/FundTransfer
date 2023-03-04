package io.dnpn.fundtransfer.transfer.api;

import io.dnpn.fundtransfer.transfer.service.IllegalTransferException;
import io.dnpn.fundtransfer.transfer.service.TransferFailureException;
import io.dnpn.fundtransfer.transfer.service.TransferRequest;
import io.dnpn.fundtransfer.transfer.service.TransferService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * We do not test null account IDs or amount in the request as they will be caught by the JSON deserializer and
 * thrown as {@link org.springframework.http.converter.HttpMessageConversionException}.
 */
@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    private static final long DEBIT_ACCOUNT = 123;
    private static final long CREDIT_ACCOUNT = 456;
    private static final BigDecimal AMOUNT = new BigDecimal("123.45");
    private static final TransferApiRequest API_REQUEST = TransferApiRequest.builder()
            .fromAccount(DEBIT_ACCOUNT)
            .toAccount(CREDIT_ACCOUNT)
            .amount(AMOUNT)
            .build();
    private static final TransferRequest SERVICE_REQUEST = TransferRequest.builder()
            .fromAccountId(DEBIT_ACCOUNT)
            .toAccountId(CREDIT_ACCOUNT)
            .amount(AMOUNT)
            .build();
    private static final LocalDateTime NOW = LocalDateTime.of(2022, Month.APRIL, 14, 9, 50, 23);
    private static final ZoneId ZONE_ID = ZoneId.of("UTC+4");
    // Expected timestamp returned when getting the time NOW from the clock and applying the offset from the ZONE_ID
    private static final LocalDateTime EXPECTED_TIMESTAMP = LocalDateTime.of(2022, Month.APRIL, 14, 13, 50, 23);


    @Mock
    private TransferService service;
    private Clock clock;
    private TransferController controller;

    @BeforeEach
    void beforeEach() {
        this.clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZONE_ID);
        this.controller = new TransferController(service, clock);
    }

    @SneakyThrows
    @Test
    void WHEN_transfer_THEN_serviceExecutesTheTransfer() {
        controller.transfer(API_REQUEST);

        verify(service).transfer(SERVICE_REQUEST);
    }

    @Test
    void WHEN_transfer_THEN_returnOkWithRequest() {
        var response = controller.transfer(API_REQUEST);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(API_REQUEST, response.getBody().request());
        assertEquals(TransferController.SUCCESSFUL_TRANSFER_MESSAGE, response.getBody().message());
        assertEquals(EXPECTED_TIMESTAMP, response.getBody().timestamp());
    }

    @SneakyThrows
    @Test
    void GIVEN_illegalTransfer_WHEN_transfer_THEN_throwResponseStatusForBadRequest() {
        doThrow(IllegalTransferException.class).when(service).transfer(SERVICE_REQUEST);

        assertThrowsResponseStatusForBadRequest(() -> controller.transfer(API_REQUEST));
    }

    @SneakyThrows
    @Test
    void GIVEN_transferFailure_WHEN_transfer_THEN_throwResponseStatusForServerError() {
        doThrow(TransferFailureException.class).when(service).transfer(SERVICE_REQUEST);

        assertThrowsResponseStatusForServerError(() -> controller.transfer(API_REQUEST));
    }


    private void assertThrowsResponseStatusForBadRequest(Executable executable) {
        assertThrowsResponseStatus(executable, HttpStatus.BAD_REQUEST);
    }

    private void assertThrowsResponseStatusForServerError(Executable executable) {
        assertThrowsResponseStatus(executable, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SneakyThrows
    private void assertThrowsResponseStatus(Executable executable, HttpStatus expectedStatus) {
        assertThrows(ResponseStatusException.class, executable);

        try {
            executable.execute();

        } catch (ResponseStatusException exception) {
            assertEquals(expectedStatus, exception.getStatus());
        }
    }
}