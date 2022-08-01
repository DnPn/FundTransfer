package io.dnpn.fundtransfer.transfer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * Request to transfer funds from one account to another.
 *
 * @param fromAccount account to debit.
 * @param toAccount   account to credit.
 * @param amount      amount to be debited on the debit account.
 */
@Builder
public record TransferApiRequest(
        @Schema(example = "123")
        @Pattern(regexp = ACCOUNT_PATTERN_REGEXP)
        @JsonProperty(TransferApiField.FROM_ACCOUNT)
        @NonNull String fromAccount,

        @Schema(example = "456")
        @Pattern(regexp = ACCOUNT_PATTERN_REGEXP)
        @JsonProperty(TransferApiField.TO_ACCOUNT)
        @NonNull String toAccount,

        @Schema(example = "123.45")
        @DecimalMin(value = "0", inclusive = false)
        @JsonProperty(TransferApiField.AMOUNT)
        @NonNull BigDecimal amount
) {
    private static final String ACCOUNT_PATTERN_REGEXP = "\\d+";
}
