package io.dnpn.fundtransfer.account;

import io.dnpn.fundtransfer.currency.Currency;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

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
    @NonNull
    final long accountId;
    /**
     * The currency used for the account.
     */
    @NonNull Currency currency;
    /**
     * The balance of the account.
     */
    @NonNull BigDecimal balance;
}
