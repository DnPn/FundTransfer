package io.dnpn.fundtransfer.transfer.service;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.accessor.ExchangeRateException;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final long DEBIT_ACCOUNT_ID = 123;
    private static final long CREDIT_ACCOUNT_ID = 456;
    private static final BigDecimal AMOUNT = new BigDecimal("123.45");
    private final TransferRequest REQUEST = TransferRequest.builder()
            .fromAccountId(DEBIT_ACCOUNT_ID)
            .toAccountId(CREDIT_ACCOUNT_ID)
            .amount(AMOUNT)
            .build();

    private Account debitAccount;
    private Account creditAccount;

    @Mock
    private AccountAccessor accountAccessor;
    @Mock
    private CurrencyConversionService conversionService;
    @InjectMocks
    private TransferService transferService;

    @BeforeEach
    void beforeEach() {
        this.debitAccount = Account.builder()
                .accountId(DEBIT_ACCOUNT_ID)
                .currency(Currency.EUR)
                .balance(AMOUNT.add(BigDecimal.ONE))
                .build();
        this.creditAccount = Account.builder()
                .accountId(CREDIT_ACCOUNT_ID)
                .currency(Currency.USD)
                .balance(BigDecimal.ONE)
                .build();
    }

    @Test
    void GIVEN_nullRequest_WHEN_transfer_THEN_throwsNullPointer() {
        assertThrows(NullPointerException.class, () -> transferService.transfer(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1.23"})
    void GIVEN_invalidAmount_WHEN_transfer_THEN_throwsIllegalTransfer(String amount) {
        TransferRequest request = TransferRequest.builder()
                .amount(new BigDecimal(amount))
                .fromAccountId(DEBIT_ACCOUNT_ID)
                .toAccountId(CREDIT_ACCOUNT_ID)
                .build();

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(request));
    }

    @Test
    void GIVEN_debitAccountNotFound_WHEN_transfer_THEN_throwsIllegalTransfer() {
        doReturn(Optional.empty()).when(accountAccessor).getById(DEBIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(REQUEST));
    }

    @Test
    void GIVEN_debitAccountWithNotEnoughMoney_WHEN_transfer_THEN_throwsIllegalTransfer() {
        debitAccount.setBalance(AMOUNT.subtract(BigDecimal.ONE));
        doReturn(Optional.of(debitAccount)).when(accountAccessor).getById(DEBIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(REQUEST));
    }

    @Test
    void GIVEN_creditAccountNotFound_WHEN_transfer_THEN_throwsIllegalTransfer() {
        doReturn(Optional.of(debitAccount)).when(accountAccessor).getById(DEBIT_ACCOUNT_ID);
        doReturn(Optional.empty()).when(accountAccessor).getById(CREDIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(REQUEST));
    }

    @SneakyThrows
    @Test
    void GIVEN_exchangeRateException_WHEN_transfer_THEN_throwsTransferFailure() {
        mockValidAccountAccess();
        doThrow(ExchangeRateException.class).when(conversionService).convert(any());

        assertThrows(TransferFailureException.class, () -> transferService.transfer(REQUEST));
    }

    @SneakyThrows
    @Test
    void WHEN_transfer_THEN_executeDebit() {
        mockValidAccountAccess();
        mockAmountConversion();
        BigDecimal updatedBalance = debitAccount.getBalance().subtract(AMOUNT);

        transferService.transfer(REQUEST);

        assertEquals(updatedBalance, debitAccount.getBalance());
        verify(accountAccessor).update(debitAccount);
    }

    @SneakyThrows
    @Test
    void WHEN_transfer_THEN_executeCredit() {
        mockValidAccountAccess();
        BigDecimal convertedAmount = mockAmountConversion();
        BigDecimal updatedBalance = creditAccount.getBalance().add(convertedAmount);

        transferService.transfer(REQUEST);

        assertEquals(updatedBalance, creditAccount.getBalance());
        verify(accountAccessor).update(creditAccount);
    }

    private void mockValidAccountAccess() {
        doReturn(Optional.of(debitAccount)).when(accountAccessor).getById(DEBIT_ACCOUNT_ID);
        doReturn(Optional.of(creditAccount)).when(accountAccessor).getById(CREDIT_ACCOUNT_ID);
    }


    @SneakyThrows
    private BigDecimal mockAmountConversion() {
        BigDecimal convertedAmount = new BigDecimal("147.89");
        doReturn(convertedAmount).when(conversionService).convert(any());
        return convertedAmount;
    }
}