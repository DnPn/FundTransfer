package io.dnpn.fundtransfer.transfer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

/**
 * Response for a fund transfer. It is used to notify the client that the operation succeeded.
 *
 * @param request   the fund transfer request.
 * @param message   a message notifying of the result.
 * @param timestamp the timestamp when the operation was performed.
 */
@Builder
public record TransferApiResponse(
        @JsonProperty(TransferApiField.REQUEST)
        @NonNull TransferApiRequest request,

        @Schema(example = TransferController.SUCCESSFUL_TRANSFER_MESSAGE)
        @JsonProperty(TransferApiField.MESSAGE)
        @NonNull String message,

        @JsonProperty(TransferApiField.TIMESTAMP)
        @NonNull LocalDateTime timestamp) {
}