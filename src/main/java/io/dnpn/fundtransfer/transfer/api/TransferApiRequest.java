package io.dnpn.fundtransfer.transfer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dnpn.fundtransfer.common.MoneyHandling;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;

import javax.validation.constraints.DecimalMin;
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
        @JsonProperty(TransferApiField.FROM_ACCOUNT)
        long fromAccount,

        @Schema(example = "456")
        @JsonProperty(TransferApiField.TO_ACCOUNT)
        long toAccount,

        @Schema(example = "123.45")
        @DecimalMin(value = "0", inclusive = false)
        @JsonProperty(TransferApiField.AMOUNT)
        @JsonSerialize(using = MoneyHandling.Serializer.class)
        @JsonDeserialize(using = MoneyHandling.Deserializer.class)
        @NonNull BigDecimal amount
) {
    private static final String ACCOUNT_PATTERN_REGEXP = "\\d+";
}
