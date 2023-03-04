package io.dnpn.fundtransfer.transfer.service;

import io.dnpn.fundtransfer.account.AccountEntity;
import io.dnpn.fundtransfer.account.AccountService;
import io.dnpn.fundtransfer.common.MoneyHandling;
import io.dnpn.fundtransfer.currency.Currency;
import io.dnpn.fundtransfer.currency.service.CurrencyConversionException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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

    private AccountEntity debitAccountEntity;
    private AccountEntity creditAccountEntity;

    @Mock
    private AccountService accountService;
    @Mock
    private CurrencyConversionService conversionService;
    @InjectMocks
    private TransferService transferService;

    @BeforeEach
    void beforeEach() {
        this.debitAccountEntity = AccountEntity.builder()
                .id(DEBIT_ACCOUNT_ID)
                .version(123)
                .currency(Currency.GBP)
                .balance(AMOUNT.add(BigDecimal.ONE))
                .build();
        this.creditAccountEntity = AccountEntity.builder()
                .id(CREDIT_ACCOUNT_ID)
                .version(456)
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
        doReturn(Optional.empty()).when(accountService).getById(DEBIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(REQUEST));
    }

    @Test
    void GIVEN_debitAccountWithNotEnoughMoney_WHEN_transfer_THEN_throwsIllegalTransfer() {
        debitAccountEntity.setBalance(AMOUNT.subtract(BigDecimal.ONE));
        doReturn(Optional.of(debitAccountEntity)).when(accountService).getById(DEBIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(REQUEST));
    }

    @Test
    void GIVEN_creditAccountNotFound_WHEN_transfer_THEN_throwsIllegalTransfer() {
        doReturn(Optional.of(debitAccountEntity)).when(accountService).getById(DEBIT_ACCOUNT_ID);
        doReturn(Optional.empty()).when(accountService).getById(CREDIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(REQUEST));
    }

    @Test
    void GIVEN_sameAccount_WHEN_transfer_THEN_throwsIllegalTransfer() {
        TransferRequest request = TransferRequest.builder()
                .amount(AMOUNT)
                .fromAccountId(DEBIT_ACCOUNT_ID)
                .toAccountId(DEBIT_ACCOUNT_ID)
                .build();
        doReturn(Optional.of(debitAccountEntity)).when(accountService).getById(DEBIT_ACCOUNT_ID);

        assertThrows(IllegalTransferException.class, () -> transferService.transfer(request));
    }

    @SneakyThrows
    @Test
    void GIVEN_currencyConversionException_WHEN_transfer_THEN_throwsTransferFailure() {
        mockValidAccountAccess();
        doThrow(CurrencyConversionException.class).when(conversionService).convert(any());

        assertThrows(TransferFailureException.class, () -> transferService.transfer(REQUEST));
    }

    @SneakyThrows
    @Test
    void WHEN_transfer_THEN_executeDebit() {
        mockValidAccountAccess();
        mockAmountConversion();

        transferService.transfer(REQUEST);

        BigDecimal updatedBalance = debitAccountEntity.getBalance().subtract(AMOUNT);
        debitAccountEntity.setBalance(updatedBalance);
        verify(accountService).update(debitAccountEntity);
    }

    @SneakyThrows
    @Test
    void WHEN_transfer_THEN_executeCredit() {
        mockValidAccountAccess();
        BigDecimal convertedAmount = mockAmountConversion();
        BigDecimal convertedAmountScaled = convertedAmount.setScale(MoneyHandling.SCALE_FOR_MONEY,
                MoneyHandling.ROUNDING_MODE_FOR_CLIENT_CREDIT);

        transferService.transfer(REQUEST);

        BigDecimal updatedBalance = creditAccountEntity.getBalance().add(convertedAmountScaled);
        creditAccountEntity.setBalance(updatedBalance);
        verify(accountService).update(creditAccountEntity);
    }

    private void mockValidAccountAccess() {
        doReturn(Optional.of(debitAccountEntity)).when(accountService).getById(DEBIT_ACCOUNT_ID);
        doReturn(Optional.of(creditAccountEntity)).when(accountService).getById(CREDIT_ACCOUNT_ID);
    }


    @SneakyThrows
    private BigDecimal mockAmountConversion() {
        BigDecimal convertedAmount = new BigDecimal("147.8952475");
        doReturn(convertedAmount).when(conversionService).convert(any());
        return convertedAmount;
    }
}