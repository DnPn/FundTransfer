package io.dnpn.fundtransfer.transfer.api;

import io.dnpn.fundtransfer.common.annotation.VisibleForTesting;
import io.dnpn.fundtransfer.transfer.service.IllegalTransferException;
import io.dnpn.fundtransfer.transfer.service.TransferFailureException;
import io.dnpn.fundtransfer.transfer.service.TransferRequest;
import io.dnpn.fundtransfer.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Controller allowing to transfer funds.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransferController {

    @VisibleForTesting
    static final String SUCCESSFUL_TRANSFER_MESSAGE = "Transfer successful";

    private final TransferService service;
    private final Clock clock;

    /**
     * Transfers funds from one account to the other.
     *
     * @param request the fund transfer request.
     * @return a response indicating if the operation was successful.
     */
    @Operation(description = "Executes a fund transfer from one account to another.", responses =
            {
                    @ApiResponse(responseCode = "200", description = "Transfer is successful.",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TransferApiResponse.class))}),

                    @ApiResponse(responseCode = "400", description = "Invalid request.",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(example = "No account found with the identifier XYZ."))})
            })
    @PostMapping("/transfer")
    public ResponseEntity<TransferApiResponse> transfer(@RequestBody TransferApiRequest request) {
        log.info("Transfer request: {}", request);

        executeTransfer(request);

        final TransferApiResponse response = TransferApiResponse.builder()
                .request(request)
                .timestamp(LocalDateTime.now(clock))
                .message(SUCCESSFUL_TRANSFER_MESSAGE)
                .build();
        return ResponseEntity.ok(response);
    }

    private TransferRequest toServiceRequest(TransferApiRequest apiRequest) {
        final long fromAccountId = convertToLong(TransferApiField.FROM_ACCOUNT, apiRequest.fromAccount());
        final long toAccountId = convertToLong(TransferApiField.TO_ACCOUNT, apiRequest.toAccount());

        return TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(apiRequest.amount())
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

    private void executeTransfer(TransferApiRequest request) {
        try {
            final TransferRequest serviceRequest = toServiceRequest(request);
            service.transfer(serviceRequest);

        } catch (IllegalTransferException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);

        } catch (TransferFailureException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        }
    }
}
