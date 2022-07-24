package io.dnpn.fundtransfer.transfer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;

/**
 * Request to transfer funds from one account to another.
 *
 * @param fromAccount account to debit.
 * @param toAccount   account to credit.
 * @param amount      amount to be debited on the debit account.
 */
@Builder
public record TransferApiRequest(
        @JsonProperty(TransferApiField.FROM_ACCOUNT) @NonNull String fromAccount,
        @JsonProperty(TransferApiField.TO_ACCOUNT) @NonNull String toAccount,
        @JsonProperty(TransferApiField.AMOUNT) @NonNull String amount) {
}
