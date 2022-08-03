package io.dnpn.fundtransfer.account;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dnpn.fundtransfer.common.MoneyHandling;
import io.dnpn.fundtransfer.currency.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;


/**
 * A simple bank account.
 */
@Builder
@Data
public class Account {
    /**
     * The unique identifier of the account.
     */
    @Schema(example = "123")
    @Positive
    final long accountId;
    /**
     * The currency used for the account.
     */
    @Schema(example = "USD")
    @Pattern(regexp = "[A-Z]{3}")
    @NonNull Currency currency;
    /**
     * The balance of the account.
     */
    @Schema(example = "123.45")
    @DecimalMin(value = "0", inclusive = false)
    @JsonSerialize(using = MoneyHandling.Serializer.class)
    @NonNull BigDecimal balance;
}
