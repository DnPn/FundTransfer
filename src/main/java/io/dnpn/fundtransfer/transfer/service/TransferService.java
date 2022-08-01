package io.dnpn.fundtransfer.transfer.service;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionRequest;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

/**
 * Service to perform a fund transfer between 2 accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountAccessor accountAccessor;
    private final CurrencyConversionService conversionService;

    /**
     * Transfers funds from one account to another. This method is transactional, if any part of its execution fails
     * then the whole operation is rolled-back.
     *
     * @param request the transfer request.
     * @throws IllegalTransferException if the requested transfer is invalid.
     * @throws TransferFailureException if the requested transfer is valid but the execution failed.
     */
    @Transactional
    public void transfer(@NonNull TransferRequest request) throws IllegalTransferException, TransferFailureException {
        final BigDecimal debitedAmount = request.amount();
        assertValidAmount(debitedAmount);
        log.debug("Transfer amount {} is valid.", debitedAmount);

        final Account debitAccount = getAccountById(request.fromAccountId());
        assertSufficientBalance(debitAccount, debitedAmount);
        log.debug("The balance of the debit account is sufficient for the transfer.");

        final Account creditAccount = getAccountById(request.toAccountId());
        assertDifferentAccounts(debitAccount, creditAccount);
        final BigDecimal creditedAmount = calculateDebitedAmount(debitAccount, creditAccount, debitedAmount);

        debitAccount(debitAccount, debitedAmount);
        creditAccount(creditAccount, creditedAmount);
        log.debug("Transfer completed: {} {} debited from the account {} | {} {} credited to the account {}",
                debitedAmount, debitAccount.getCurrency(), debitAccount.getAccountId(),
                creditedAmount, creditAccount.getCurrency(), creditAccount.getAccountId()
        );
    }

    private Account getAccountById(long accountId) throws IllegalTransferException {
        return accountAccessor.getById(accountId)
                .orElseThrow(() -> supplyAccountNotFoundException(accountId));
    }

    private IllegalTransferException supplyAccountNotFoundException(long accountId) {
        final String message = String.format("No account found with the identifier %d.", accountId);
        return new IllegalTransferException(message);
    }

    /**
     * Valid that the amount for the transfer is valid. For now this method checks just that the amount is positive,
     * but we may in the future check that it does not exceed a maximum allowed amount or that it has no more than 2
     * decimals.
     *
     * @param amount the amount to validate.
     * @throws IllegalTransferException if the amount is invalid.
     */
    private void assertValidAmount(BigDecimal amount) throws IllegalTransferException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            final String message = String.format("Invalid transfer amount: %.2f. The amount must be positive.", amount);
            throw new IllegalTransferException(message);
        }
    }

    private void assertSufficientBalance(Account debitedAccount, BigDecimal debitedAmount) throws IllegalTransferException {
        if (debitedAmount.compareTo(debitedAccount.getBalance()) > 0) {
            final String message = String.format("Invalid transfer of %.2f %s from the account %s: the amount exceeds" +
                    " the balance.", debitedAmount, debitedAccount.getCurrency(), debitedAccount.getAccountId());
            throw new IllegalTransferException(message);
        }
    }

    private void assertDifferentAccounts(Account debitAccount, Account creditAccount) throws IllegalTransferException {
        if (debitAccount.getAccountId() == creditAccount.getAccountId()) {
            final String message = String.format("The same account %d was chosen as a debit and credit account, " +
                    "please choose different accounts to make a transfer.", debitAccount.getAccountId());
            throw new IllegalTransferException(message);
        }
    }

    private BigDecimal calculateDebitedAmount(Account debitAccount, Account creditAccount, BigDecimal debitedAmount) throws TransferFailureException {
        try {
            final CurrencyConversionRequest request = CurrencyConversionRequest.builder()
                    .amount(debitedAmount)
                    .fromCurrency(debitAccount.getCurrency())
                    .toCurrency(creditAccount.getCurrency())
                    .build();
            return conversionService.convert(request);

        } catch (CurrencyConversionException exception) {
            final String message = String.format("Unsupported currency conversion from %s to %s.",
                    debitAccount.getCurrency(), creditAccount.getCurrency());
            log.error(message, exception);
            throw new TransferFailureException(message, exception);
        }
    }

    private void debitAccount(Account account, BigDecimal amount) {
        final BigDecimal newBalance = account.getBalance().subtract(amount);
        updateAccountBalance(account, newBalance);
    }

    private void creditAccount(Account account, BigDecimal amount) {
        final BigDecimal newBalance = account.getBalance().add(amount);
        updateAccountBalance(account, newBalance);
    }

    private void updateAccountBalance(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountAccessor.update(account);
    }
}
