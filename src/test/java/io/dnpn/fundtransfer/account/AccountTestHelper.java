package io.dnpn.fundtransfer.account;

import io.dnpn.fundtransfer.currency.Currency;

import java.math.BigDecimal;

public final class AccountTestHelper {

    public static final long ID_ACCOUNT_A = 123;
    public static final Currency CURRENCY_ACCOUNT_A = Currency.GBP;
    public static final BigDecimal BALANCE_ACCOUNT_A = new BigDecimal("100.50");
    public static final Account ACCOUNT_A = Account.builder()
            .accountId(ID_ACCOUNT_A)
            .balance(BALANCE_ACCOUNT_A)
            .currency(CURRENCY_ACCOUNT_A)
            .build();

    public static final long ID_ACCOUNT_B = 456;
    public static final Currency CURRENCY_ACCOUNT_B = Currency.USD;
    public static final BigDecimal BALANCE_ACCOUNT_B = new BigDecimal("787.62");
    public static final Account ACCOUNT_B = Account.builder()
            .accountId(ID_ACCOUNT_B)
            .balance(BALANCE_ACCOUNT_B)
            .currency(CURRENCY_ACCOUNT_B)
            .build();

    private AccountTestHelper() {
    }
}
