package io.dnpn.fundtransfer.transfer.service;

import io.dnpn.fundtransfer.account.AccountEntity;
import io.dnpn.fundtransfer.account.AccountService;
import io.dnpn.fundtransfer.common.MoneyHandling;
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

    private final AccountService accountService;
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
        final var debitedAmount = request.amount();
        assertValidAmount(debitedAmount);
        log.debug("Transfer amount {} is valid.", debitedAmount);

        final var debitAccount = getAccountById(request.fromAccountId());
        assertSufficientBalance(debitAccount, debitedAmount);
        log.debug("The balance of the debit account is sufficient for the transfer.");

        final var creditAccount = getAccountById(request.toAccountId());
        assertDifferentAccounts(debitAccount, creditAccount);
        final BigDecimal creditedAmount = calculateDebitedAmount(debitAccount, creditAccount, debitedAmount);

        debitAccount(debitAccount, debitedAmount);
        creditAccount(creditAccount, creditedAmount);
        log.debug("Transfer completed: {} {} debited from the account {} | {} {} credited to the account {}",
                debitedAmount, debitAccount.getCurrency(), debitAccount.getId(),
                creditedAmount, creditAccount.getCurrency(), creditAccount.getId()
        );
    }

    private AccountEntity getAccountById(long accountId) throws IllegalTransferException {
        return accountService.getById(accountId)
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

    private void assertSufficientBalance(AccountEntity debitedAccount, BigDecimal debitedAmount) throws IllegalTransferException {
        if (debitedAmount.compareTo(debitedAccount.getBalance()) > 0) {
            final String message = String.format("Invalid transfer of %.2f %s from the account %s: the amount exceeds" +
                    " the balance.", debitedAmount, debitedAccount.getCurrency(), debitedAccount.getId());
            throw new IllegalTransferException(message);
        }
    }

    private void assertDifferentAccounts(AccountEntity debitAccount, AccountEntity creditAccount) throws IllegalTransferException {
        if (debitAccount.getId() == creditAccount.getId()) {
            final String message = String.format("The same account %d was chosen as a debit and credit account, " +
                    "please choose different accounts to make a transfer.", debitAccount.getId());
            throw new IllegalTransferException(message);
        }
    }

    private BigDecimal calculateDebitedAmount(AccountEntity debitAccount, AccountEntity creditAccount, BigDecimal debitedAmount) throws TransferFailureException {
        try {
            final var request = CurrencyConversionRequest.builder()
                    .amount(debitedAmount)
                    .fromCurrency(debitAccount.getCurrency())
                    .toCurrency(creditAccount.getCurrency())
                    .build();
            final var convertedAmount = conversionService.convert(request);
            return scaleConvertedAmountToMoney(convertedAmount);

        } catch (CurrencyConversionException exception) {
            final String message = String.format("Unsupported currency conversion from %s to %s.",
                    debitAccount.getCurrency(), creditAccount.getCurrency());
            log.error(message, exception);
            throw new TransferFailureException(message, exception);
        }
    }

    private BigDecimal scaleConvertedAmountToMoney(BigDecimal amount) {
        return amount.setScale(MoneyHandling.SCALE_FOR_MONEY, MoneyHandling.ROUNDING_MODE_FOR_CLIENT_CREDIT);
    }

    private void debitAccount(AccountEntity account, BigDecimal amount) {
        final BigDecimal newBalance = account.getBalance().subtract(amount);
        updateAccountBalance(account, newBalance);
    }

    private void creditAccount(AccountEntity account, BigDecimal amount) {
        final BigDecimal newBalance = account.getBalance().add(amount);
        updateAccountBalance(account, newBalance);
    }

    private void updateAccountBalance(AccountEntity account, BigDecimal newBalance) {
        account.setBalance(newBalance);
        accountService.update(account);
    }
}
