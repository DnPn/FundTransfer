package io.dnpn.fundtransfer.transfer.service;

import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

/**
 * Request to transfer funds.
 *
 * @param fromAccountId account to be debited.
 * @param toAccountId   account to be credited.
 * @param amount        amount to be debited from the debit account.
 */
@Builder
public record TransferRequest(
        long fromAccountId,
        long toAccountId,
        @NonNull BigDecimal amount
) {
}
