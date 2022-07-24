package io.dnpn.fundtransfer.transfer.api;

import io.dnpn.fundtransfer.transfer.service.TransferRequest;
import io.dnpn.fundtransfer.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Controller allowing to transfer funds.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransferController {

    private static final String SUCCESSFUL_TRANSFER_MESSAGE = "Transfer successful";

    private final TransferService service;

    /**
     * Transfers funds from one account to the other.
     *
     * @param request the fund transfer request.
     * @return a response indicating if the operation was successful.
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransferApiResponse> transfer(@RequestBody TransferApiRequest request) {
        log.info("Transfer request: {}", request);

        final TransferRequest serviceRequest = toServiceRequest(request);
        service.transfer(serviceRequest);

        final TransferApiResponse response = TransferApiResponse.builder()
                .request(request)
                .timestamp(LocalDateTime.now())
                .message(SUCCESSFUL_TRANSFER_MESSAGE)
                .build();
        return ResponseEntity.ok(response);
    }

    private TransferRequest toServiceRequest(TransferApiRequest apiRequest) {
        final long fromAccountId = convertToLong(TransferApiField.FROM_ACCOUNT, apiRequest.fromAccount());
        final long toAccountId = convertToLong(TransferApiField.TO_ACCOUNT, apiRequest.toAccount());
        final BigDecimal amount = convertToBigDecimal(TransferApiField.AMOUNT, apiRequest.amount());

        return TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();
    }

    private long convertToLong(String fieldName, String fieldValue) {
        try {
            return Long.parseLong(fieldValue);

        } catch (NumberFormatException exception) {
            final String message = String.format("Invalid value <%s> for the field <%s>: must be a valid integer.",
                    fieldValue, fieldName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message, exception);
        }
    }

    private BigDecimal convertToBigDecimal(String fieldName, String fieldValue) {
        try {
            return new BigDecimal(fieldValue);

        } catch (NumberFormatException exception) {
            final String message = String.format("Invalid value <%s> for the field <%s>:  must be a valid decimal " +
                            "number.",
                    fieldValue, fieldName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message, exception);
        }
    }
}
